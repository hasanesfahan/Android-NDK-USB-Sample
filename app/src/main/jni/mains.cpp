#include <string.h>
#include <jni.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>

#include <sys/ioctl.h>
#include <linux/usbdevice_fs.h>

#define RAND_F_SSLEAY_RAND_BYTES

int endPointIn;
int endPointOut;
int fdesc;

extern "C" {


JNIEXPORT void JNICALL Java_ir_bigandsmall_hiddevice_CSepronik_OpenDevice
		(JNIEnv *env,jobject thiz, jint jfdesc, jint jEndPointIn, jint jEndPointOut);



JNIEXPORT jshort JNICALL  Java_ir_bigandsmall_hiddevice_CSepronik_ReadMemory
		(JNIEnv *env,jclass clazz , jclass buffer ,jint len );


JNIEXPORT jshort JNICALL  Java_ir_bigandsmall_hiddevice_CSepronik_WriteMemory
		(JNIEnv *env,jobject thiz ,jstring buffer,jint len  );



}


static int ReadHID( void *buffer, int len)
{
    memset(buffer , 0,len);
    struct usbdevfs_bulktransfer bt;
    bt.ep = 134;  //endpoint (received from Java)
    bt.len = len ;      //      length of data
    bt.timeout = 1000;    //  timeout in ms
    bt.data = buffer;        //the data

    return ioctl(fdesc, USBDEVFS_BULK , &bt);
}

static int WriteHID(void *buffer, int len)
{
    struct usbdevfs_bulktransfer bt;
    bt.ep = 7;  /* endpoint (received from Java) */
    bt.len = len;            /* length of data */
    bt.timeout = 4000;      /* timeout in ms */
    bt.data = buffer;        /* the data */
    return ioctl(fdesc, USBDEVFS_BULK, &bt);
}

JNIEXPORT void JNICALL Java_ir_bigandsmall_hiddevice_CSepronik_OpenDevice  (JNIEnv *env , jobject thiz ,
        jint jfdesc, jint jEndPointIn, jint jEndPointOut) {
    endPointIn = jEndPointIn;
    endPointOut = jEndPointOut;
    fdesc = jfdesc;
}

JNIEXPORT jshort JNICALL  Java_ir_bigandsmall_hiddevice_CSepronik_WriteMemory  (JNIEnv *env,jobject thiz ,
											jstring buffer,jint plainLength  ) {

	const char *constDataPlain = env->GetStringUTFChars( buffer, NULL);

	char *plainBuffer = (char*)malloc(plainLength/2);

	strcpy(plainBuffer, constDataPlain);
	int tmp = 0;
	for(int i=0;i<plainLength/2;i++) {
		sscanf(plainBuffer + (2 * i), "%2x", &tmp);
		*(plainBuffer + i) = tmp;
	}
	return WriteHID((void *)plainBuffer , plainLength/2);
}

JNIEXPORT jshort JNICALL  Java_ir_bigandsmall_hiddevice_CSepronik_ReadMemory
		(JNIEnv *env,jclass clazz ,  jclass buffer ,jint len ){
	clazz = env->GetObjectClass (buffer);
	jmethodID mid = env->GetMethodID (clazz, "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");

    char * byteRead  = new char[len];
    //BYTE * byteRead  = new BYTE[len];
	int retValue = ReadHID((void *)byteRead,len);

	char readModule[len*2+1] , *readPointer;
	int i = 0,p=0;
	readPointer= readModule;

	int tmp = 0 ;
	for(i=0;i<len;i++)
		sprintf(readPointer+i*2, "%02x", byteRead[i]);
	readPointer[i*2-2] = '\0';

	env->CallObjectMethod (buffer, mid, env->NewStringUTF((const char *) readPointer));
	return  retValue;
}
