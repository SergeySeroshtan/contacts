# Summary

This application allows synchronize contacts in smartphone with information from corporate data sources.

Application takes information about employees from corporate directory service.
Furthermore, it uses database to store additional information about employees.

# Modules

The [Web Module][module:web] collects contacts from DS and DB and provides access to them through REST API.
The [Android App][module:android] synchronizes contacts on Android smartphones using this REST API.
In addition, web module provides web interface to access contacts using browser.

[module:web]: http://github.com/grytsenko/contacts/blob/master/modules/contacts-web
[module:android]: http://github.com/grytsenko/contacts/blob/master/modules/contacts-android
