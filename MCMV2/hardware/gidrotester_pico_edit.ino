#include <Arduino.h>
#include <SPI.h>



// analogWrite(pinToUse, dutycycle);
//////////////////////////////////////////////
#define BUSY1 29           //
 #define BUSY2 13          //
 #define BUSY3 10          // 
#define RESET 26            
#define START_CONVERSION 27    //4
#define SS1 28        //5
#define SS2 11        //5
#define SS3 9        //5
#define MISO 12      //rx on board      
#define SCK 14       //15-ошибка      
#define MOSI 15     //tx14
#define TOTAL_RAW_BYTES 16
#define SOF1 0xA5
#define SOF2 0x5A
#define FRAME_SIZE 29
#define PAYLOAD_SIZE 24
#define TYPE_PRESSURE 0x01
#define TYPE_CURRENT 0x02
#define TYPE_START 0x10
#define TYPE_END 0x11

const uint8_t FRAME_SOF1 = 0xA5;
const uint8_t FRAME_SOF2 = 0x5A;
const uint8_t FRAME_TYPE_PRESSURE = 0x01;
const uint8_t FRAME_TYPE_CURRENT = 0x02;
const uint8_t FRAME_TYPE_START = 0x10;
const uint8_t FRAME_TYPE_END = 0x11;
const uint8_t FRAME_PAYLOAD_SIZE = 24;
const uint8_t FRAME_SIZE = 29;

//uint8_t adcn;
 
int bytesToRead = TOTAL_RAW_BYTES;
int16_t raw1[TOTAL_RAW_BYTES];
int16_t raw2[TOTAL_RAW_BYTES];
int16_t raw3[TOTAL_RAW_BYTES];
int16_t parsed1[8];
int16_t parsed2[8];
int16_t parsed3[8];
//uint16_t parsed[16];
////////////////////////////////////////////////////////////////////////////

int16_t test;



#define inbits 14         // число значений в массиве, который хотим получить

//byte receivenew;
byte indexsend;
bool recievedflag;
bool sendingflag;
bool startflag;
bool stopflag;
bool pwmflag;
bool zeroflag;
bool stepscounterflag;
bool curmode;



uint8_t indata[inbits]; // входящие данные
byte pressures[24]; // массив давлений на отправку
byte currents[24];  // массив токов на отправку
uint8_t zeroPayload24[PAYLOAD_SIZE] = {0};
byte pwms [12];
uint8_t txSeq = 0;
<<<<<<< Updated upstream
uint8_t zeroPayload[FRAME_PAYLOAD_SIZE] = {0};
=======
>>>>>>> Stashed changes

uint16_t freq;
uint16_t hz;
uint8_t hz1;
uint8_t hz2;
uint8_t analog1;
uint8_t analog2;

uint8_t out [2000][14];
uint16_t timers [2000];
uint16_t grad [2000];
//uint16_t out[10];
//uint16_t gradient;


uint32_t adress;

//uint16_t steps;
uint16_t stepscounter;
 uint16_t stepscounterold;


//byte timer [3];
unsigned long totaltime;


 unsigned long previousstarttimer=0;
 unsigned long previousstarttimersteps=0;
 

unsigned long sendtimer = 0;
const long sendinterval = 2000;  // задержка между пакетами!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

unsigned long previousMillis = 0;      
const long interval = 1000;
//sizeof(incomingByte)

uint8_t crc8(const uint8_t* data, size_t len) {
  uint8_t crc = 0x00;
  for (size_t i = 0; i < len; i++) {
    crc ^= data[i];
    for (uint8_t bit = 0; bit < 8; bit++) {
      if (crc & 0x80) {
        crc = (uint8_t)((crc << 1) ^ 0x07);
      } else {
        crc <<= 1;
      }
    }
  }
  return crc;
}

<<<<<<< Updated upstream
void sendFrame(uint8_t type, const uint8_t* payload) {
  uint8_t frame[FRAME_SIZE];
  frame[0] = FRAME_SOF1;
  frame[1] = FRAME_SOF2;
  frame[2] = type;
  frame[3] = txSeq++;
  for (uint8_t i = 0; i < FRAME_PAYLOAD_SIZE; i++) {
    frame[4 + i] = payload[i];
  }
  frame[FRAME_SIZE - 1] = crc8(&frame[2], 26);
  Serial.write(frame, FRAME_SIZE);
}

void sendingpress() { //отправка давлений
  sendFrame(FRAME_TYPE_PRESSURE, pressures);
}

void sendingcurr(){  //отправка токов отправляется после 4х отправок давлений
  sendFrame(FRAME_TYPE_CURRENT, currents);
=======
void sendFrame(uint8_t type, const uint8_t* payload24) {
  uint8_t frame[FRAME_SIZE];
  uint8_t crcInput[2 + PAYLOAD_SIZE];

  frame[0] = SOF1;
  frame[1] = SOF2;
  frame[2] = type;
  frame[3] = txSeq;
  crcInput[0] = type;
  crcInput[1] = txSeq;

  for (uint8_t i = 0; i < PAYLOAD_SIZE; i++) {
    frame[4 + i] = payload24[i];
    crcInput[2 + i] = payload24[i];
  }

  frame[FRAME_SIZE - 1] = crc8(crcInput, sizeof(crcInput));
  Serial.write(frame, FRAME_SIZE);
  txSeq++;
}

void sendingpress() { //отправка давлений
sendFrame(TYPE_PRESSURE, pressures);
}

void sendingcurr(){  //отправка токов отправляется после 4х отправок давлений
 sendFrame(TYPE_CURRENT, currents);
>>>>>>> Stashed changes
}



void pressuretransform(uint8_t index, uint16_t val, uint8_t type){ // преобразование давлений для отправки
 uint8_t ext;
 uint8_t a;
 index=index << 1;
 index=constrain(index, 0, 23);
 if(type==0){
 val=map(val,0,32767,0,4095);
 }
 
 val=constrain(val, 0, 4095);
 a = val >> 8;
  if(val<=255){
    ext=val; 
  }else if (val>255 && val<=511){
    ext=val-256;
  }else if (val>511 && val<=767){
    ext=val-512;
  }else if (val>767 && val<=1023){
    ext=val-768;
  }else if (val>1023 && val<=1279){
    ext=val-1024;
  }else if (val>1279 && val<=1535){
    ext=val-1280;
  }else if (val>1535 && val<=1791){
    ext=val-1536;
  }else if (val>1791 && val<=2047){
    ext=val-1792;
  }else if (val>2047 && val<=2303){
    ext=val-2048;
  }else if (val>2303 && val<=2559){
    ext=val-2304;
  }else if (val>2559 && val<=2815){
    ext=val-2560;
  }else if (val>2815 && val<=3071){
    ext=val-2816;
  }else if (val>3071 && val<=3327){
    ext=val-3072;
  }else if (val>3327 && val<=3583){
    ext=val-3328;
  }else if (val>3583 && val<=3839){
    ext=val-3584;
  }else if (val>3839 && val<=4095){
    ext=val-3840; 
  }
pressures[index]= ext;
index++;
pressures[index]= a;
}

void currtransform(uint8_t index1, uint16_t val1, uint8_t type1){ // преобразование curr для отправки
 uint8_t ext1;
 uint8_t a1;
 uint8_t num=index1;
 index1=index1 << 1;
 index1=constrain(index1, 0, 23);
 if(type1==0){
 val1=map(val1,0,32767,0,4095);
 }
 
 val1=constrain(val1, 0, 4095);
 //intake[num]=val1;
 a1 = val1 >> 8;
 a1=a1+16;
  if(val1<=255){
    ext1=val1; 
  }else if (val1>255 && val1<=511){
    ext1=val1-256;
  }else if (val1>511 && val1<=767){
    ext1=val1-512;
  }else if (val1>767 && val1<=1023){
    ext1=val1-768;
  }else if (val1>1023 && val1<=1279){
    ext1=val1-1024;
  }else if (val1>1279 && val1<=1535){
    ext1=val1-1280;
  }else if (val1>1535 && val1<=1791){
    ext1=val1-1536;
  }else if (val1>1791 && val1<=2047){
    ext1=val1-1792;
  }else if (val1>2047 && val1<=2303){
    ext1=val1-2048;
  }else if (val1>2303 && val1<=2559){
    ext1=val1-2304;
  }else if (val1>2559 && val1<=2815){
    ext1=val1-2560;
  }else if (val1>2815 && val1<=3071){
    ext1=val1-2816;
  }else if (val1>3071 && val1<=3327){
    ext1=val1-3072;
  }else if (val1>3327 && val1<=3583){
    ext1=val1-3328;
  }else if (val1>3583 && val1<=3839){
    ext1=val1-3584;
  }else if (val1>3839 && val1<=4095){
    ext1=val1-3840; 
  }
currents[index1]= ext1;
index1++;
currents[index1]= a1;
}




void sensread(){ //чтение аналоговых входов и отправка данных

 pressuretransform(0,parsed2[4],0);
 pressuretransform(1,parsed2[5],0);
 pressuretransform(2,parsed2[6],0);
 pressuretransform(3,parsed2[7],0);
 pressuretransform(4,parsed3[0],0);
 pressuretransform(5,parsed3[1],0);
 pressuretransform(6,parsed3[2],0);
 pressuretransform(7,parsed3[3],0);
 pressuretransform(8,parsed3[4],0);
 pressuretransform(9,parsed3[5],0);
 pressuretransform(10,parsed3[6],0);
 pressuretransform(11,parsed3[7],0);
// pressuretransform(6,rpmin0,1);
 //pressuretransform(7,adc.analogRead(7),1);
 
 if (micros() - sendtimer >= sendinterval) {
    sendtimer+=sendinterval;
indexsend++;
 if (indexsend==4){
  indexsend=0;
 //currtransform(0,test,1);/////test parsed1[0]
 currtransform(0,parsed1[0],0);
 currtransform(1,parsed1[1],0);
 currtransform(2,parsed1[2],0);
 currtransform(3,parsed1[3],0);
 currtransform(4,parsed1[4],0);
 currtransform(5,parsed1[5],0);
 currtransform(6,parsed1[6],0);
 currtransform(7,parsed1[7],0);
  currtransform(8,parsed2[0],0);
 currtransform(9,parsed2[1],0);
 currtransform(10,parsed2[2],0);
 currtransform(11,parsed2[3],0);
  sendingcurr();
 }else{ sendingpress();
 }
 }  

}


void parsing() {//парсинг входящих данных
  if (Serial.available() > 0) {//слушаем ком порт
    Serial.readBytes(indata, inbits);
recievedflag=1;
  }
 
if (recievedflag==1&&indata[0]==116){//старт данных
  sendingflag=1;
  recievedflag=0;
}

if (recievedflag==1&&indata[0]==120&&indata[1]==0&&indata[2]==0){
  //delay(1000);
startflag=1;
previousstarttimer = millis();

//previousstarttimersteps= millis();/////////////////////////////////////// можно упростить
zeroflag=1;

//SpiRam.read_ints(0,out,10);

sendtimer= micros();

<<<<<<< Updated upstream
sendFrame(FRAME_TYPE_START, zeroPayload);
=======
sendFrame(TYPE_START, zeroPayload24);
>>>>>>> Stashed changes

  recievedflag=0;
}

if (recievedflag==1&&indata[0]==120&&indata[1]==138&&indata[2]==2||stopflag==1&&startflag==0){
  //sendingflag=0;
  stopflag=0;
<<<<<<< Updated upstream
sendFrame(FRAME_TYPE_END, zeroPayload);
=======
sendFrame(TYPE_END, zeroPayload24);
>>>>>>> Stashed changes
  recievedflag=0;
}
if (recievedflag==1&&indata[0]==84){//стоп данных, обнуление времени
  sendingflag=0;
  totaltime = 0;
  recievedflag=0;
  test=0;
}
if (recievedflag==1&&indata[0]==113){
pwms[0]=indata[1];
pwms[1]=indata[2];
pwms[2]=indata[3];
pwms[3]=indata[4];
pwms[4]=indata[5];
pwms[5]=indata[6];
pwms[6]=indata[7];
pwms[7]=indata[8];
pwms[8]=indata[9];
pwms[9]=indata[10];
pwms[10]=indata[11];
pwms[11]=indata[12];
  pwmflag=1;
  recievedflag=0;
}
if (recievedflag==1&&indata[0]==81){
 //pwms[8]=indata[1];
//pwms[9]=indata[2];
//pwms[10]=indata[3];
//pwms[11]=indata[4];
analog1=indata[1];
analog2=indata[2];
//pwms[6]=indata[5];
//pwms[7]=indata[7];
  pwmflag=1; 
  recievedflag=0;
}


//СЦЕНАРИЙ!!!!!!

if (recievedflag==1&&indata[0]==114){
  test++;
unsigned int steps;
unsigned int gradient;
steps=indata[2]<< 8;
steps=steps+indata[1];

gradient=indata[10]<< 8;
gradient=gradient+indata[9];

out [steps][8]=indata[3];
out [steps][9]=indata[4];
out [steps][10]=indata[5];
out [steps][11]=indata[6];
out [steps][12]=indata[7];
out [steps][13]=indata[8];
grad [steps]= gradient;

recievedflag=0;
}

if (recievedflag==1&&indata[0]==115){
 test++;  
  unsigned int steps;
  unsigned int intime;
  
intime = indata[12] << 8;
intime = intime+indata[11];

steps=indata[2]<< 8;
steps=steps+indata[1];

timers[steps]=intime;

out [steps][0]=indata[3];
out [steps][1]=indata[4];
out [steps][2]=indata[5];
out [steps][3]=indata[6];
out [steps][4]=indata[7];
out [steps][5]=indata[8];
out [steps][6]=indata[9];
out [steps][7]=indata[10];


totaltime = totaltime+intime;
recievedflag=0;
}

if (recievedflag==1&&indata[0]==104){
//hz1=indata[2]; 
//hz2=indata[1];
//hz=1000000/((hz1*256)+hz2);


hz=indata[2]<< 8;
hz=hz+indata[1];
hz=constrain(hz, 25, 5000);
//pwmController.setPWMFrequency(hz);
  recievedflag=0;
}

   
}

void pwmsend() {

analogWrite(25, map((pwms[0]),0,100,0,255));
analogWrite(24, map((pwms[1]),0,100,0,255));
analogWrite(23, map((pwms[2]),0,100,0,255));
analogWrite(22, map((pwms[3]),0,100,0,255));
analogWrite(21, map((pwms[4]),0,100,0,255));
analogWrite(20, map((pwms[5]),0,100,0,255));
analogWrite(6,  map((pwms[6]),0,100,0,255));
analogWrite(19, map((pwms[7]),0,100,0,255));
analogWrite(7, map((pwms[8]),0,100,0,255));
analogWrite(18, map((pwms[9]),0,100,0,255));
analogWrite(8,  map((pwms[10]),0,100,0,255));
analogWrite(17, map((pwms[11]),0,100,0,255));

}




void setup() {
Serial.begin(500000);
  //Serial.println("GO");
initial();

   
  //while (!Serial) {}  // wait for usb connection
 SPI1.setRX(MISO);
 //SPI1.setCS(SS1);
 SPI1.setSCK(SCK);
 SPI1.setTX(MOSI);
  SPI1.begin(true);

  delay(500);
 
freq=2500;
analogWriteFreq(freq);
analogWriteResolution(8);



for (byte i = 1; i < 16; i=i+2) {
  currents[i]=16;
}

 currents[0]=255;

hz=55;
//sendingflag=1;
}


//-----------------------------------------
void initial() {
  pinMode(BUSY1, INPUT);
  pinMode(BUSY2, INPUT);
  pinMode(BUSY3, INPUT);
  pinMode(RESET, OUTPUT);
  pinMode(START_CONVERSION, OUTPUT);
  pinMode(SS1, OUTPUT);
  pinMode(SS2, OUTPUT);
  pinMode(SS3, OUTPUT);
  //pinMode(MISO, OUTPUT);
  pinMode(SCK, OUTPUT);

pinMode(25, OUTPUT);
pinMode(24, OUTPUT);
pinMode(23, OUTPUT);
pinMode(22, OUTPUT);
pinMode(21, OUTPUT);
pinMode(20, OUTPUT);
pinMode(6, OUTPUT);
pinMode(19, OUTPUT);
pinMode(7, OUTPUT);
pinMode(18, OUTPUT);
pinMode(8, OUTPUT);
pinMode(17, OUTPUT);

pinMode(2, OUTPUT);
pinMode(3, OUTPUT);

 pinMode(4, OUTPUT);
  
  digitalWrite(START_CONVERSION, LOW);
  digitalWrite(SS1, HIGH);
  digitalWrite(SS2, HIGH);
  digitalWrite(SS3, HIGH);
  reset(RESET);
}
//-----------------------------------------
void parseRawBytes() {
   for (int i = 0; i < 8; i++) {
    parsed1[i] = (raw1[i * 2] << 8) + raw1[(i * 2) + 1];
    parsed2[i] = (raw2[i * 2] << 8) + raw2[(i * 2) + 1];
    parsed3[i] = (raw3[i * 2] << 8) + raw3[(i * 2) + 1];
  
  // parsed[i] = raw1[i];
  //  curin[4]=map(parsed1[4],16130,29491,0,5000);
   
  }
}
//-----------------------------------------
/*reset signal*/
void reset(uint8_t port) {
  digitalWrite(port, HIGH);
  // delayMicroseconds(1);
  digitalWrite(port, LOW);
  //  delayMicroseconds(1);
}
//-----------------------------------------
void conversionPulse(uint8_t port) {
  digitalWrite(port, LOW);
  //  delayMicroseconds(1);
  digitalWrite(port, HIGH);
}
//-----------------------------------------

bool waitBusyLow(uint8_t pin, uint32_t timeoutUs = 3000) {
  uint32_t t0 = micros();
  while (digitalRead(pin) == HIGH) {
    if ((uint32_t)(micros() - t0) >= timeoutUs) {
      return false;
    }
  }
  return true;
}

void readData() {
  //////////////////////////////////////////////////////////1
  conversionPulse(START_CONVERSION);
  if (!waitBusyLow(BUSY1)) {
    return;
  }
  SPI1.beginTransaction(SPISettings(26000000, MSBFIRST, SPI_MODE2));   
  digitalWrite(SS1, LOW);
  while (bytesToRead > 0) {
    raw1[TOTAL_RAW_BYTES - bytesToRead] = SPI1.transfer(0x00);
    bytesToRead--;
  }
  digitalWrite(SS1, HIGH);
  SPI1.endTransaction();
  bytesToRead = TOTAL_RAW_BYTES;
 ////////////////////////////////////////////////////////////2 
conversionPulse(START_CONVERSION);
  if (!waitBusyLow(BUSY2)) {
    return;
  }
  SPI1.beginTransaction(SPISettings(26000000, MSBFIRST, SPI_MODE2));   
  digitalWrite(SS2, LOW);
  while (bytesToRead > 0) {
    raw2[TOTAL_RAW_BYTES - bytesToRead] = SPI1.transfer(0x00);
    bytesToRead--;
  }
  digitalWrite(SS2, HIGH);
  SPI1.endTransaction();
  bytesToRead = TOTAL_RAW_BYTES;
//////////////////////////////////////////////////////////////
conversionPulse(START_CONVERSION);
  if (!waitBusyLow(BUSY3)) {
    return;
  }
  SPI1.beginTransaction(SPISettings(26000000, MSBFIRST, SPI_MODE2));   
  digitalWrite(SS3, LOW);
  while (bytesToRead > 0) {
    raw3[TOTAL_RAW_BYTES - bytesToRead] = SPI1.transfer(0x00);
    bytesToRead--;
  }
  digitalWrite(SS3, HIGH);
  SPI1.endTransaction();
  bytesToRead = TOTAL_RAW_BYTES;
//////////////////////////////////////////////////////////////
}






void loop() {
readData();
parseRawBytes();
//if (startflag==1){
//if (millis() - previousstarttimer >= totaltime) { 
//    startflag=0;
//    stopflag=1;}}

if (startflag==1){

 
 if (millis() - previousstarttimersteps >= timers[stepscounter]) {
if(zeroflag==0){
stepscounter++;
}

analogWrite(25,out[stepscounter][0]);
analogWrite(24,out[stepscounter][1]);
analogWrite(23,out[stepscounter][2]);
analogWrite(22,out[stepscounter][3]);
analogWrite(21,out[stepscounter][4]);
analogWrite(20,out[stepscounter][5]);
analogWrite (6,out[stepscounter][6]);
analogWrite(19,out[stepscounter][7]);
analogWrite(7, out[stepscounter][8]);
analogWrite(18,out[stepscounter][9]);
analogWrite(8, out[stepscounter][10]);
analogWrite(17, out[stepscounter][11]);


previousstarttimersteps= millis();
if(zeroflag==1){
 stepscounter=0;
 zeroflag=0; 
}
 }
  
if (millis() - previousstarttimer >= totaltime) { 
    startflag=0;
    stopflag=1;
    stepscounter=0;}}
    
 
 parsing();

 if (sendingflag==1){
 sensread();
 }
 
if (pwmflag==1){
 pwmsend();
 pwmflag=0;  
}

}
