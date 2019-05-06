# mis-2019-exercise-3b-opencv
3b Bonus:
Following an online instruction we had little problems setting up openCV. The only problem was accidentaly downloading a wrong
sdk(probably just not designed for android) and needing to resetup the project with the correct one. We followed the tutorial on:
https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c

3b:
detectMultiScale returns an array of rectangles, in the case of faces they are squares. For determining the position and size 
of the nose, we calculated the center of the square and an edge length. We found, that the nose is best positioned 10% of an 
edge length below the center, aswell as with a radius of 10% of an edge length. Using the changing property of the size of the
rectangle, the size of the nose is adapted dynamically. 
