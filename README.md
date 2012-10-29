WreckRoll
=========

Control system for the 2012 CoC Wreck Parade remote control car.  This project consists of 4 parts:

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


This project was pulled together by several contributers over about a week, with
most of the serious development occurring within the last 2 days/nights.  Even
though the parade is over, development will continue to make a general purpose
control system for car, plane, hexapod robot, etc.

