/*
  Ethernet Monitor Analogo
 By: http://www.elprofegarcia.com/
 Este programa usa el Modulo Ethernet de Arduino para monitorear las entradas analogicas A0-A5
 
 Conexiones:
 * Ethernet shield usa los pins 10, 11, 12, 13
 * Monitorea los pines Analogos de A0 a A5
 
Se debe conectar el modulo a la red local y se debe asignar una IP fija que no coincida
con los equipos de la red que ya estan funcionando pero dede estar dentro de la SubRed.
puede monitorear la IP de su PC dentro de la ventana de comandos CMD con el comando ipconfig
*/

#include <SPI.h>
#include <Ethernet.h>
#include "DHT.h"
#include "pitches.h"
#define DHTTYPE DHT22
                               // Introduzca una dirección MAC y la dirección IP para el controlador

byte mac[] = { 
0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
IPAddress ip(192,168,0,50);   // Esta direccion IP debe ser cambiada obligatoriamente 
                              // dependiendo de la subred de su Area Local y es la que 
                              // usara para conectarse por el Navegador.
byte gateway[] = { 192, 168, 0, 1 };                   // Puerta de enlace
byte subnet[] = { 255, 255, 255, 0 };                  //Mascara de Sub Red
EthernetServer server(80);    // Puerto 80 por defecto para HTTP
String readString;

int melody[] = {
  NOTE_C4, NOTE_G3, NOTE_G3, NOTE_A3, NOTE_G3, 0, NOTE_B3, NOTE_C4
};

// note durations: 4 = quarter note, 8 = eighth note, etc.:
int noteDurations[] = {
  4, 8, 8, 4, 4, 4, 4, 4
};

const int pinLED = 9;
const int BUZZERPin= 3;
const int PIRPin= 2;
const int DHTPin= 5;
const int pinrojo = 6;
const int pinverde = 7;
const int pinazul = 8;
void MedirDHT11(float temperatura);
void Alarma(float humedad);
DHT dht(DHTPin, DHTTYPE); 
void setup() {
  Ethernet.begin(mac, ip);    //inicializa la conexión Ethernet y el servidor
  server.begin();
  Serial.begin(9600);
  Serial.println("DHTxx test!");
 pinMode(pinrojo, OUTPUT);  //definir pin como salida
  pinMode(pinverde, OUTPUT);
  pinMode(pinazul, OUTPUT);
  pinMode(pinLED, OUTPUT);

  pinMode(BUZZERPin, OUTPUT);
  pinMode(PIRPin, INPUT);
  pinMode(DHTPin, INPUT);
dht.begin();
   pinMode(BUZZERPin, OUTPUT);
  pinMode(PIRPin, INPUT);
}

void loop() {
  delay(500);
  int value= digitalRead(PIRPin);
 Alarma(value);
 float h = dht.readHumidity();
  float t = dht.readTemperature();
  Serial.print("Humidity: ");
  float humedad=h/100;
  Serial.print(humedad);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
float temperatura=((t-32)/18.8);
  Serial.print(temperatura);
  Serial.print(" °C\t");
  Serial.print("\n ");
  MedirDHT11(temperatura);
  EthernetClient cliente = server.available(); // Inicializa cliente como servidor ethernet
  if (cliente) {
    boolean currentLineIsBlank = true;
    while (cliente.connected()) {
      if (cliente.available()) {
        char c = cliente.read();
         if (readString.length() < 100) {
          //Almacena los caracteres a un String
          readString += c;
          
         }

        if (c == '\n' && currentLineIsBlank) { 
          cliente.println("HTTP/1.1 200 OK");
          cliente.println("Content-Type: text/html");   // Envia el encabezado en codigo HTML estandar
          cliente.println("Connection: close"); 
    cliente.println("Refresh: 3");  // refresca la pagina automaticamente cada 3 segundos
          cliente.println();
          cliente.println("<!DOCTYPE HTML>"); 
          cliente.println("<html>");
          cliente.println("<HEAD>");
          cliente.println("<TITLE>Ethernet Monitor</TITLE>");
          cliente.println("</HEAD>");
          cliente.println("<BODY>");
          cliente.println("<hr />");
          cliente.println("<H1>Arduino Monitor</H1>");
          cliente.println("<br />");  
          cliente.println("<H2>Monitorea PIR</H2>");
          cliente.println("<br />");
          
          cliente.println("< br />");
          //cliente.println("<a href=\"/?button1\"\"> Ver Tempertura</a> ");           // construye en la pagina cada uno de los botones  
                     cliente.println("TEMPERATURA: ");
               cliente.println(temperatura);  
          cliente.println(" | | | ");
                     cliente.println("HUMEDAD: ");
               cliente.println(humedad);  
          //cliente.println("<a href=\"/?button2\"\"> Ver Humedad</a> ");           // construye en la pagina cada uno de los botones
          cliente.println("< br />");
          
          cliente.println("Lectura: "); 
          
          if(value){
             cliente.print("Se detecto movimiento");
             cliente.println("< br />");       
          }
          else{
             cliente.print("sin movimiento");
             cliente.println("< br />");       
          }
         
          cliente.println("<br />"); 
          cliente.println("Sistemas Operativos Avanzados"); 
          cliente.println("</BODY>");
          cliente.println("</html>");
          break;
        }
        if (c == '\n') {
           currentLineIsBlank = true;
        } 
        else if (c != '\r') {
           currentLineIsBlank = false;
        }
      }
    }
   delay(15);           // Da tiempo al Servidor para que reciba los datos 15ms
   cliente.stop();     // cierra la conexion
   /*if (readString.indexOf("?button1") >0){
               cliente.println("Temperatura:");
               cliente.println(temperatura);  
           }
           if (readString.indexOf("?button2") >0){
               cliente.println("Humedad:");
               cliente.println(humedad);  
           }*/
   if (readString.indexOf("APAGAR") >0){
               digitalWrite(pinLED, LOW);
               digitalWrite(BUZZERPin, LOW);
           }
  readString="";  
  }
}



















//**************************************************************************************************
//*                                                                                                *
//*   FUNCIONES                                                                                    *
//*                                                                                                *
//*                                                                                                *
//**************************************************************************************************



void MedirDHT11(float temp)
{int caso=0;
  if(temp >=25){
    caso=1;
  }
  if(temp > 14&& temp<25){
    caso=2;
  }
  if(temp <= 14){
    caso=3;
  }
 switch (caso) {
  case 1:
    digitalWrite(pinverde, LOW);   // poner el Pin en HIGH
     digitalWrite(pinazul,LOW);   // poner el Pin en HIGH
     digitalWrite(pinrojo, HIGH);   // poner el Pin en HIGH
    // statements
    break;
  case 2:
    digitalWrite(pinverde, HIGH);   // poner el Pin en HIGH
     digitalWrite(pinazul,LOW);   // poner el Pin en HIGH
     digitalWrite(pinrojo, LOW);   // poner el Pin en HIGH
    // statements
    break;
  case 3:
  digitalWrite(pinverde, LOW);   // poner el Pin en HIGH
     digitalWrite(pinazul,HIGH);   // poner el Pin en HIGH
     digitalWrite(pinrojo, LOW);   // poner el Pin en HIGH 
    // statements
  break;
  default:
  {}
  break;
} 
}

void Alarma(int value)
{
  if (value == HIGH)
  {  digitalWrite(pinLED,HIGH);   // poner el Pin en HIGH
  for (int thisNote = 0; thisNote < 8; thisNote++) {

    // to calculate the note duration, take one second
    // divided by the note type.
    //e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
    int noteDuration = 1000 / noteDurations[thisNote];
    tone(3, melody[thisNote], noteDuration);

    // to distinguish the notes, set a minimum time between them.
    // the note's duration + 30% seems to work well:
    int pauseBetweenNotes = noteDuration * 1.30;
    delay(pauseBetweenNotes);
    // stop the tone playing:
    noTone(3);
  }
  /*
  digitalWrite(BUZZERPin, HIGH);
    delay(50);
    digitalWrite(BUZZERPin, LOW);
    delay(50);*/
  }
  else
  {
    digitalWrite(BUZZERPin, LOW);
  } 
}

