# Dummy Json Project for Android
A basic application for fetching dummy user data from https://dummyjson.com/docs/users and deleting, editing or adding to that data. I used OkHTTP for http requests, and Jackson for parsing JSON. Jetpack Compose was used for the UI-elements.
## Motivation
The project was a part of a native mobile development course.
## Installation
Download one of the zip-files from the releases (ideally the latest one), extract the files from the zip, and use android studio to run the application.
## How to use?
Once the application launches you it automatically gets all users from the from the dummyjson-database. The buttons on the bottom of the screen allow you to add users, search for users or get all users. By pressing on the three dots you can edit or delete users. All changes stay until the activity is closed.
### Adding users
After pressing the add-button, there are five fields (first name, last name, age, phone, email) you must fill. After all the fields are filled, press the add-button in the lower left. If you want to leave the "Add user"-dialog, you can press outside the dialog or press the cancel-button. After pressing "add" and waiting for the load, you should find your user at the end of the list. 
### Searching for users
After pressing the search-button, you can search for users based on name and email. After searching you can see every user that matches your search term. Added users are included and edited users will also show their edited information.
### Get all
Application gets all users when the application is started and after adding, deleting or editing users. There is also the get all-button at the bottom right of the application, useful for getting all users after a search.
### Editing users
After pressing the three dots on the user cards, a dialog will appear that will let you change any users name, age, phone or email. None of these fields can be empty or the edit won't be accepted. Press the "edit user"-button in the lower to edit the user information. You can edit added users, and all edits persist until the activity is closed.
### Deleting users
In the same dialog that you can edit users, you can delete that user by pressing the "delete user"-button. You can delete edited and added users and all deletions persist until the activity is closed.
## ScreenCast
placeholder text
## Final thoughts
I feel much more comfortable with Jetpack Compose after this project, as well as doing http requests and parsing that data. I'm also pretty happy with how the UI turned out, I managed to smooth out most of the rough edges in the project. There are some things I still left undone due to difficulty or laziness. The edited/deleted/added users being saved if the instance was closed (but not destroyed) was one of those features, but I felt like adding that was way more complicated than I was initially expecting, so I left that undone. Overall, I give it a pretty good/10.