/*
 * SPI testing utility (using spidev driver)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * cross compile with arm-linux-gnueabihf-gcc -o spi_test -lrt -lpthread spi_latency.c
 */

#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>
#include <linux/spi/spidev.h>
#include <time.h>
#include <pthread.h>
#include <string.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof((a)[0]))


int check_equal(uint8_t* first, uint8_t* second, unsigned int len)
{
	for(int i=0; i<len; i++){
		if(first[i] != second[i])
			return 1;
	}

	return 0;
}


void timespec_diff(struct timespec *start, struct timespec *stop,
                   struct timespec *result)
{
    if ((stop->tv_nsec - start->tv_nsec) < 0) {
        result->tv_sec = stop->tv_sec - start->tv_sec - 1;
        result->tv_nsec = stop->tv_nsec - start->tv_nsec + 1000000000;
    } else {
        result->tv_sec = stop->tv_sec - start->tv_sec;
        result->tv_nsec = stop->tv_nsec - start->tv_nsec;
    }

    return;
}

static void pabort(const char *s)
{
	perror(s);
	abort();
}


static const char *device = "/dev/spidev0.0";
static uint8_t mode = 3;
static uint8_t bits = 8;
static uint32_t speed = 1000000;
static uint16_t delay = 0;
static uint16_t iterations = 1;
int fd = -1;
static int threaded = false;

uint8_t test_data1[] = {
		0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
		0x40, 0x00, 0x00, 0x00, 0x00, 0x95,
		0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
		0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
		0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
		0xDE, 0xAD, 0xBE, 0xEF, 0xBA, 0xAD,
		0xF0, 0x0D, 0xEE, 0x00,
};

uint8_t test_data2[] = {
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA, 0xAA, 0xAA,
		0xAA, 0xAA, 0xAA, 0xAA,
};


/* TODO: Better would be a struct called spi_transfer
 * including following attributes:
 * tx_data
 * rx_data
 * len
 * struct timespec latency
 *
 * Then encapsulating an array of spi_transfer in test_context
 */
struct test_context {
	const char* ident;
	uint8_t *tx_data;
	uint8_t *rx_data;
	int len;
	struct timespec *latencies;
};

static void transfer(struct test_context* context)
{
	int ret;
	int i;
	struct timespec start, end, diff, *time_diffs;
	struct spi_ioc_transfer *tr;
	uint8_t* rx;

	tr = (struct spi_ioc_transfer*) malloc(sizeof(struct spi_ioc_transfer));
	if(!tr)
		pabort("Can't malloc");

	rx = (uint8_t*) malloc(context->len);
	if(!rx)
		pabort("Can't malloc");

	time_diffs = (struct timespec*) malloc(iterations*sizeof(struct timespec));
	if(!time_diffs)
		pabort("Can't malloc");


	tr->tx_buf 			= (unsigned long)context->tx_data;
	tr->rx_buf 			= (unsigned long)rx;
	tr->len    			= context->len;
	tr->delay_usecs 	= delay;
	tr->speed_hz 		= 0;
	tr->bits_per_word 	= 0;

	context->latencies = time_diffs;

	ret = clock_getres( CLOCK_MONOTONIC, &start);
	if(ret)
		pabort("can't retreive clock resolution");
	else
		printf( "%s Timerresolution in ns: %ld\n",
				context->ident,
				start.tv_nsec );



	for(i=0; i< iterations; i++){

		/* time critical begins */
		/* force scheduling */
		sched_yield();
		clock_gettime(CLOCK_MONOTONIC, &start);
		ret = ioctl(fd, SPI_IOC_MESSAGE(1), tr);
		clock_gettime(CLOCK_MONOTONIC, &end);
		timespec_diff(&start, &end, &diff);
		/* time critical ends */

		memcpy(&(context->latencies[i]), &diff, sizeof(struct timespec));

		if (ret < 0)
			pabort("can't send spi message");

	}

	for(i=0; i< iterations; i++){
		printf("%s: Time for sending %d bytes: ", context->ident,context->len);
		printf("%s: Transfer %d, Seconds: %ld, Nanoseconds:%ld\n",
				context->ident,
				i,
				context->latencies[i].tv_sec,
				context->latencies[i].tv_nsec);
	}

#if DEBUG
	printf("Thread 1:\n");
	for (ret = 0; ret < context->len; ret++) {
		if (!(ret % 6))
			puts("");
		printf("%.2X ", rx[ret]);
	}
	puts("");
#endif

	free(tr);
	free(rx);
	free(time_diffs);

}

void* thread1(void* args)
{
	transfer((struct test_context*)args);
}

void* thread2(void *args)
{
	transfer((struct test_context*)args);
}

void print_usage(const char *prog)
{
	printf("Usage: %s [-DsbdlHOLC3it]\n", prog);
	puts("  -D --device   device to use (default /dev/spidev0.0)\n"
	     "  -s --speed    max speed (Hz)\n"
	     "  -d --delay    delay (usec)\n"
	     "  -b --bpw      bits per word \n"
	     "  -l --loop     loopback\n"
	     "  -H --cpha     clock phase\n"
	     "  -O --cpol     clock polarity\n"
	     "  -L --lsb      least significant bit first\n"
	     "  -C --cs-high  chip select active high\n"
	     "  -3 --3wire    SI/SO signals shared\n"
		 "  -i --iterations iterations of write cycles\n"
		 "  -t --threaded   activate threaded test. This will test if spidev is threadsafe\n");
	exit(1);
}

void parse_opts(int argc, char *argv[])
{
	while (1) {
		static const struct option lopts[] = {
			{ "device",  1, 0, 'D' },
			{ "speed",   1, 0, 's' },
			{ "delay",   1, 0, 'd' },
			{ "bpw",     1, 0, 'b' },
			{ "loop",    0, 0, 'l' },
			{ "cpha",    0, 0, 'H' },
			{ "cpol",    0, 0, 'O' },
			{ "lsb",     0, 0, 'L' },
			{ "cs-high", 0, 0, 'C' },
			{ "3wire",   0, 0, '3' },
			{ "iterations", 1, 0, 'i'},
			{ "threaded", 0, 0, 't'},
			{ NULL, 0, 0, 0 },
		};
		int c;

		c = getopt_long(argc, argv, "D:s:d:b:i:lHOLC3t", lopts, NULL);

		if (c == -1)
			break;

		switch (c) {
		case 'D':
			device = optarg;
			break;
		case 's':
			speed = atoi(optarg);
			break;
		case 'd':
			delay = atoi(optarg);
			break;
		case 'b':
			bits = atoi(optarg);
			break;
		case 'l':
			mode |= SPI_LOOP;
			break;
		case 'H':
			mode |= SPI_CPHA;
			break;
		case 'O':
			mode |= SPI_CPOL;
			break;
		case 'L':
			mode |= SPI_LSB_FIRST;
			break;
		case 'C':
			mode |= SPI_CS_HIGH;
			break;
		case '3':
			mode |= SPI_3WIRE;
			break;
		case 'i':
			iterations = atoi(optarg);
			break;
		case 't':
			threaded = true;
			break;
		default:
			print_usage(argv[0]);
			break;
		}
	}
}

int main(int argc, char *argv[])
{
	int ret = 0;
	int i;
	struct test_context context1, context2;
	pthread_t tid[2];

	parse_opts(argc, argv);

	fd = open(device, O_RDWR);
	if (fd < 0)
		pabort("can't open device");

	/*
	 * spi mode
	 */
	ret = ioctl(fd, SPI_IOC_WR_MODE, &mode);
	if (ret == -1)
		pabort("can't set spi mode");

	ret = ioctl(fd, SPI_IOC_RD_MODE, &mode);
	if (ret == -1)
		pabort("can't get spi mode");

	/*
	 * bits per word
	 */
	ret = ioctl(fd, SPI_IOC_WR_BITS_PER_WORD, &bits);
	if (ret == -1)
		pabort("can't set bits per word");

	ret = ioctl(fd, SPI_IOC_RD_BITS_PER_WORD, &bits);
	if (ret == -1)
		pabort("can't get bits per word");

	/*
	 * max speed hz
	 */
	ret = ioctl(fd, SPI_IOC_WR_MAX_SPEED_HZ, &speed);
	if (ret == -1)
		pabort("can't set max speed hz");

	ret = ioctl(fd, SPI_IOC_RD_MAX_SPEED_HZ, &speed);
	if (ret == -1)
		pabort("can't get max speed hz");

	printf("spi mode: %d\n", mode);
	printf("bits per word: %d\n", bits);
	printf("max speed: %d Hz (%d KHz)\n", speed, speed/1000);
	context1.tx_data = test_data1;
	context1.ident   = "thread1";
	context1.len  = ARRAY_SIZE(test_data1);

	if (threaded) {
		context2.tx_data = test_data2;
		context2.len  = ARRAY_SIZE(test_data2);
		context2.ident   = "thread2";
		pthread_create(&(tid[0]), NULL, &thread1, (void*) &context1);
		pthread_create(&(tid[1]), NULL, &thread2, (void*) &context2);

		for (i=0; i<2; i++){
			if (pthread_join(tid[i], NULL))
			  pabort("can't join thread");
		}
	} else {
		transfer(&context1);
	}

	close(fd);

	return ret;
}
