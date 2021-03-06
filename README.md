# Brain Food: A News Reader & Aggregator for Android powered by Hacker News

This project is a lightweight application to view the best, top, and newest stories from Hacker News for the Android platform (6.0 and above). It supports filtering by category and refreshing. Each story item provides title, url, author, timestamp as well as attempts to show the best image possible (still a work-in-progress, images aren't provided in the JSON payloads for hacker news so I have to scrape HTML). Tapping into a given story will allow a fullscreen preview of the full text. I hope to add saving / liking and a settings page too along with a bunch of other features, and eventually release it to the Google Play store.

### Written in Kotlin & XML
### 3rd Party Libraries: OkHttp, Glide, jSoup
### Android architecture: AndroidX, Material, Room, Lifecycle, View Model, Observers, LiveData and Co-Routines 

#### Known Bug: Opening the application without a network connection yields an empty screen (Internet required currently)
#### Known Bug: Image quality on some of the story items has poor resolution quality
#### Known Bug: Long load times (~10-15 seconds) in-between refreshes, it's due to HTML scraping for images :[ optimizations on the way
#### Author: Matt Devoto

Do not reproduce these works without the expressed written consent of Matt Devoto
