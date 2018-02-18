
#include <jni.h>
#include <cpu-features.h>
#include "Deco.h"

jint Java_com_project_mp3singroom_decoding_decode(JNIEnv *env, jobject thiz,  jstring filename)
{
	const jbyte *str;
	int result;
	str = (*env)->GetStringUTFChars(env, filename, NULL);

	result = decode(str);

	(*env)->ReleaseStringUTFChars(env, filename, str);

	return result;
}
