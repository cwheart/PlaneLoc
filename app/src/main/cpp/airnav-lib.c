#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <android/log.h>
#include <string.h>
#include <math.h>

#define TAG   "custom"
//#define HOST  "192.168.1.100"
//#define HOST  "192.168.0.110"
#define HOST "192.168.1.77"
//#define HOST "192.168.1.104"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__) // 定义LOGF类型

#define BIT_SET(x, y) x|=(1<<(y)) //将X的第Y位置1
#define BUFF_SIZE 29

#define FL 100.0*0.3804

int fd;
struct sockaddr_in sockaddr_in1;
struct sockaddr_in cli_addr;

// submit location data to server
// plane_no 机号
// tail_no 尾号
// altitude 海拔
// latitude 经度
// longitude 纬度
// pressure 气压
// pressure_altitude 通过气压计算的海拔
// speed 时速
int post_data(int len, unsigned char buf[]) // 160或150
{
    if(!fd) {
        fd = socket(AF_INET, SOCK_DGRAM, 0);
        sockaddr_in1.sin_family = AF_INET;
        sockaddr_in1.sin_port = htons(8000);
        sockaddr_in1.sin_addr.s_addr = inet_addr(HOST);
    }

    unsigned char send_buf[29];
    for (int i = 0; i < 27; ++i) {
        send_buf[i] = buf[i];
    }
    sendto(fd, &send_buf, len, 0, (struct sockaddr*)&sockaddr_in1, sizeof(sockaddr_in1));
//    close(fd);
    __android_log_print(ANDROID_LOG_INFO, "custom", "send data............");
    return 0;
}

void set_int(unsigned char buf[], int data, int start) {
    for (int i = 0; i < 4; ++i) {
        int base = pow(256, (3-i));
        buf[start + i] = data / base;
        data = data % base;
    }
}

void set_short_int(unsigned char buf[], short int data, int start) {
    for (int i = 0; i < 2; ++i) {
        int base = pow(256, (1-i));
        buf[start + i] = data / base;
        data = data % base;
    }
}

// 4个字节
int set_acFs(unsigned char uacBuff[]) {
    char acFs[4];
    int j;
    unsigned short int iFsLen = 0;
    BIT_SET(acFs[0], 4); // 经维度 130
    BIT_SET(acFs[0], 3); // 唯一编号 080
    BIT_SET(acFs[0], 2); // 海拔 140
    BIT_SET(acFs[1], 5); // 气压高度 145
    BIT_SET(acFs[1], 4); // 速度 150
    BIT_SET(acFs[2], 4); // 机号 170
    BIT_SET(acFs[0], 0);
    BIT_SET(acFs[1], 0);
//    BIT_SET(acFs[2], 0);
//    BIT_SET(acFs[3], 0);
    for(j=0;j<4;j++) {
        if(acFs[j]>0) {
            uacBuff[3 + j] = acFs[j];
            iFsLen = iFsLen + 1;
        }
    }
    return iFsLen;
}

// 3个字节
int set_header(unsigned char uacBuff[], unsigned short int begin) {
    uacBuff[0] = 0x15; // 头 0x15
    begin = htons(begin);
    memcpy(uacBuff + 1, &begin, sizeof(unsigned short int)); // 长度 包括头本身数据长度
}


// 唯一编号 8080
// 3个字节
int set_target(unsigned char buf[], unsigned short int begin, int target) {
    int iPos = 3;
    for (int i = 0; i < 3; ++i) {
        int base = pow(256, (2-i));
        buf[begin + i] = target / base;
        target = target % base;
    }
    return iPos;
}

// 机号 170
// 6个字节
int set_plane_no(unsigned char uacBuff[], unsigned short int begin, const unsigned char plane_no[]) {
    unsigned short int usT1;
    int j;
    int iPos = 0;
    unsigned char uacT3[6];
    unsigned char uacTemp[BUFF_SIZE];

    uacT3[0] = plane_no[0]; //xx000000  uacT3[0]=uacT3[0]<<2; //000000xx  uacT3[0]=uacT3[0]|plane_no[1]>>4; //00000000
    uacT3[1] = plane_no[1] << 4; //00000000 0000  uacT3[1]=uacT3[1]|plane_no[2]>>2;//00000000 00000000
    uacT3[2] = plane_no[2] << 6; //00000000 00000000 0000  uacT3[2]=uacT3[2]|plane_no[3];
    uacT3[3] = plane_no[4];
    uacT3[3] = uacT3[3] << 2;
    uacT3[3] = uacT3[3]|plane_no[5] >> 4;
    uacT3[4] = plane_no[5]<<4;
    uacT3[4] = uacT3[4]|plane_no[6]>>2;
    uacT3[5] = plane_no[6]<<6;
    uacT3[5] = uacT3[5]|plane_no[7];
    for(j=0; j<6; j++) {
        uacTemp[iPos++] = uacT3[j];
    }
    memcpy(uacBuff + begin, uacTemp, iPos);
    return  iPos;
}

// 海拔 140
// 2个字节
int set_altitude(unsigned char uacBuff[], unsigned short int begin, float altitude) {
    short int int_alt = (short int) (altitude / 6.25 / 0.3048);
    set_short_int(uacBuff, int_alt, begin);
    int iPos = sizeof(short int);
    return iPos;
}


// 经维度 130
// 8个字节
int set_wgs(unsigned char uacBuff[], unsigned short int begin, float latitude, float longitude) {
    int int_lat = (int)(latitude * pow(2, 25) / 180);
    int int_long = (int)(longitude * pow(2, 25) / 180);

    int iPos = (int)sizeof(int);
    set_int(uacBuff, int_lat, begin);
    set_int(uacBuff, int_long, begin + iPos);
    return iPos + iPos;
}


// 气压高度 145
// 2个字节
int set_pressure(unsigned char uacBuff[], unsigned short int begin, float pressure) {
    short int int_pressure = (short int)(pressure / 0.25 / (100.0*0.3804));
    set_short_int(uacBuff, int_pressure, begin);
    int iPos = sizeof(short int);
    return iPos;
}

// 速度 160或150
// 2个字节
int set_speed(unsigned char uacBuff[], unsigned short int begin, float speed) {
    int iPos = sizeof(short int);
    short int int_speed = (short int)(speed * pow(2, 14) / 1852.0);

//    BIT_SET(int_speed, 15);
    set_short_int(uacBuff, int_speed, begin);
    return iPos;
}

JNIEXPORT jstring JNICALL
Java_com_xinshuaifeng_work_planeloc_AirNav_submitData( JNIEnv* env,
                                                       jobject instance,
                                                       jstring plane_no_,
                                                       jfloat c_altitude,
                                                       jfloat c_latitude,
                                                       jfloat c_longitude,
                                                       jfloat c_pressure,
                                                       jfloat c_speed)
{

    const char *c_plane_no;
    unsigned short int begin = 3;
    unsigned short int usT1;
    c_plane_no = (*env)->GetStringUTFChars(env, plane_no_, NULL);
    unsigned char uacBuff[BUFF_SIZE];
    unsigned char plane_no[8];
    for(int i = 0; i < 8; i++) {
        plane_no[i] = *(c_plane_no + i);
    }


    begin = begin + set_acFs(uacBuff);
    unsigned int target = 8000;
    begin = begin + set_wgs(uacBuff, begin, c_latitude, c_longitude);
    begin = begin + set_target(uacBuff, begin, target);
    begin = begin + set_altitude(uacBuff, begin, c_altitude);
    begin = begin + set_pressure(uacBuff, begin, c_pressure);
    begin = begin + set_speed(uacBuff, begin, c_speed);
    begin = begin + set_plane_no(uacBuff, begin, plane_no);

    set_header(uacBuff, begin);
    post_data(sizeof(uacBuff), uacBuff);
    return (*env)->NewStringUTF(env, c_plane_no);
}

JNIEXPORT void JNICALL
Java_com_xinshuaifeng_work_planeloc_AirNav_connect(JNIEnv *env, jobject instance, jstring host_) {
    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    int yes = 1;
    int ret;

    if(fd) {
        close(fd);
        fd = socket(AF_INET, SOCK_DGRAM, 0);
        sockaddr_in1.sin_family = AF_INET;
        sockaddr_in1.sin_port = htons(15000);
        sockaddr_in1.sin_addr.s_addr = inet_addr(host);
        cli_addr.sin_family = AF_INET;
        cli_addr.sin_port = htons(9693);
        cli_addr.sin_addr.s_addr = 0;
        if ((ret = bind(fd, (struct sockaddr* )&cli_addr, sizeof(cli_addr))) < 0)
        {
            perror("bind");
        }
    }
    (*env)->ReleaseStringUTFChars(env, host_, host);
}