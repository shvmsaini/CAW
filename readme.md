# My Android Task Management Application

This is an Android application built using Kotlin that helps users manage their tasks. It features
adding, deleting, marking tasks as complete, and fetching tasks from an external API.

## Features

- **Add Tasks:** Users can add new tasks with a name and description.
- **Complete Tasks:** Users can mark tasks as completed.
- **Delete Tasks:** Users can remove tasks.
- **Fetch tasks:** the application can fetch task from an external API.
- **First launch**: the first time the application is launched some predefined tasks are added.
- **Analytics**: the application uses firebase analytics to track some user interactions.

## Libraries Used

This project leverages several libraries to enhance functionality and maintain code quality. Here's
a brief overview:

- **Jetpack Compose:**
- **Purpose:** Building the user interface (UI) using a declarative approach.
- **Why:** Enables a modern and efficient way to design UI components with Kotlin.
- **AndroidX Activity and Core KTX:**
    - **Purpose:** Core Android framework components, including activities, fragments, and extension
      functions.
    - **Why:** Provides the foundation for Android application development.
- **Material 3:**
    - **Purpose:** Implementing Material Design UI components.
    - **Why:** Ensures a consistent and modern look and feel throughout the app.
- **Lifecycle ViewModel:**
    - **Purpose:** Managing UI-related data and lifecycle.
    - **Why:** Helps in surviving configuration changes and separating UI logic from the UI layer.
- **Room Persistence Library:**
    - **Purpose:** Object-relational mapping (ORM) for SQLite databases.
    - **Why:** Simplifies database interaction and data persistence within the app.
- **Kotlin Coroutines:**
    - **Purpose:** Asynchronous programming for tasks like database operations and network requests.
    - **Why:** Improves performance and responsiveness by allowing long-running tasks to run without
      blocking the main thread.
- **DataStore:**
    - **Purpose:** store data using key value pairs.
    - **Why:** used to check if the user is using the app for the first time.
- **OkHttp:**
    - **Purpose:** Handling network requests.
    - **Why:** Efficiently communicates with web services to fetch data.
- **Gson:**
    - **Purpose:** Serializing and deserializing JSON data.
    - **Why:** Used for converting JSON responses from the API into data objects and vice versa.
- **Firebase Analytics:**
    - **Purpose:** track user behavior in the application.
    - **Why:** helps to understand user interaction and monitor network performance.