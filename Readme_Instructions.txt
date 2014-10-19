The main class that is necessary to run is >>
MainView.java

Over this screen, there are defined three tabs >>
1. ConfigurationView:
If you type an IP address that does not have the correct format and you press
"Start" button, an alert message is displayed
This validation has been done using a regular expression, in such a way that
valid IP addresses are: 255.255.255.255,0.0.0.0,127.0.0.1,... 4 numbers from [0-255]