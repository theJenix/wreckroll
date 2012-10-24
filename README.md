WreckRoll
=========

Control system for a remote control car.  This project consists of 4 parts:

registrar: Node.js service registrar.  The other components of this system will
publish their IP to the registrar, and will query for IPs of services they need.

relay: Node.js relay server.  This server sits between the Android app and
Arduino board to relay commands and statuses between the different pieces.  This
server acts as a buffer between the app and control board, and can be used to
implement command filtering, grouping, and other intelligence.

wreckroll-android: Android based controller and video receiver.  This app
displays the video from the camera as a background, and projects buttons onto
the video to use as controls for the car.

wreckroll-arduino: Arduino sketch and C code for controlling the car's
electronics.
