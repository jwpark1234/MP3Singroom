export ANDROID_ROOT=/cygdrive/c/my-android-toolchain

./configure --target-os=linux \
--arch=arm \
--enable-cross-compile \
--cc=$ANDROID_ROOT/bin/arm-linux-androideabi-gcc \
--cross-prefix=$ANDROID_ROOT/bin/arm-linux-androideabi- \
--extra-cflags="-marm -march=armv7-a -mfloat-abi=softfp -mfpu=neon" \
--extra-ldflags="-Wl,--fix-cortex-a8" \
--disable-ffmpeg \
--disable-doc \
--disable-ffplay \
--disable-ffprobe \
--disable-ffserver \
--disable-avdevice \
--disable-network \
--disable-devices \
--disable-filters
