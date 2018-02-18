#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <stdbool.h>
#include <malloc.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "Deco.h"

#ifdef HAVE_AV_CONFIG_H
#undef HAVE_AV_CONFIG_H
#endif

int decode(const char filename[]) {

	/* must be called before using avcodec lib */
	avcodec_init();

	/* register all the codecs */
	av_register_all();

	const char *outfilename = "/mnt/sdcard/out.pcm"; /* 디코딩이 끝난 음원이 저장될 위치  */

	AVCodec *codec;
	AVCodecContext *c = NULL;
	AVFormatContext *pFormatCtx = NULL;
	AVPacket avpkt;

	int audioStream;
	unsigned int i;
	int out_size, len, samplerate;
	FILE *outfile;  //디코딩 된 pcm을 저장할 파일
	int16_t *outbuf;
	int32_t temp;

	av_init_packet(&avpkt); //avpkt 변수를 초기화

	if( avformat_open_input(&pFormatCtx, filename, NULL, NULL) != 0)
	{
		printf("open error\n");
	} /* 저장되어 있는 파일을 오픈 시켜 pFormatCtx에 저장 */

	if ( av_find_stream_info( pFormatCtx ) != 0)
	{
		printf("Could find stream info\n");
	} /* 파일로 부터 헤더를 읽어 오는 작업 */

	av_dump_format( pFormatCtx, 0, filename, false); /* 헤더 정보를 출력 */

	audioStream = -1;
	for(i=0 ; i < pFormatCtx->nb_streams; i++)
	{
		if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			audioStream = i;
			break;
		}
	} /* 읽어온 파일 정보로 부터 오디오 파일의 스트림 시작 부분을 찾아 옴 */

	if ( audioStream == -1 )
		exit(1); /* 오디오를 찾을 수 없으면 종료 */

	printf("Audio decoding\n");

		codec = avcodec_find_decoder( pFormatCtx->streams[audioStream]->codec->codec_id);
	if (!codec) {
		fprintf(stderr, "codec not found\n");
		exit(1);
	} /* 읽어온 오디오 정보로 부터 디코더를 찾아서 codec에 디코더 정보를 저장 */

	c = pFormatCtx->streams[audioStream]->codec;

	if ( avcodec_open(c, codec) < 0){
		fprintf(stderr, "could not open codec\n");
		exit(1);
	} /* 읽어온 디코더를 오픈 */

	samplerate = c->sample_rate;

	outbuf = malloc(AVCODEC_MAX_AUDIO_FRAME_SIZE); /* 파일에 저장하기 위한 버퍼 공간 생성 */

	outfile = fopen(outfilename, "wb");
	if(!outfile){
		av_free(c);
		exit(1);
	}

	while( av_read_frame( pFormatCtx, &avpkt) >= 0){
		int bps = av_get_bytes_per_sample(c->sample_fmt);
		out_size = FFMAX(avpkt.size * bps, AVCODEC_MAX_AUDIO_FRAME_SIZE);

		len = avcodec_decode_audio3(c, (short *)outbuf, &out_size, &avpkt);
		if( len < 0 ){
			fprintf(stderr, "Error while decoding\n");
			break;
		}

		if ( out_size > 0 ){

			for(i = 0; i < out_size; i+=2) {

				temp = outbuf[i+1] - outbuf[i];

				if(temp > 32767)
					temp = 32767;
				else if(temp < -32768)
					temp = -32768;

				outbuf[i] = temp;
				outbuf[i+1] = temp;
			}

			fwrite(outbuf, 1, out_size, outfile);
		}
		avpkt.size -= len;
		avpkt.data += len;
	}
	fclose(outfile);
	free(outbuf);

	avcodec_close(c);
	av_free(c);

	return samplerate;
}
