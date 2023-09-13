# JigsawSudoku
A portfolio project to gain and prove fullstack skills using React, Redux, Java Spring, PostgreSQL and more.

## Table of Contents

- [Overview](#overview)
- [Built With](#built-with)
- [Features](#features)
- [Contact](#contact)
- [How to build](#How-to-build)
- [How to run during developement](#How-to-run-during-developement)

## Overview
| ![Showcase of using the project.](projectImages/showcase.gif?raw=true) | 
|:--:| 
| *Showcase of using the project* |

The project is an SPA website that offers the ability to play the game Jigsaw Sudoku. If a user logs in he can also post comments, rate the game and have his score be saved.

The project started off as a semestral project for subject _Component Programming_ in second grade summer semester during my BCS studies at a university in 2022. After finishing university in 2023 I revisited the project, significantly improving the code quality, securing the website against **CSRF, XSS and SQL Injection attacks**, adding more **unit and integration tests** and fully reworking the frontend using **React, Redux, React Query, Vite, TypeScript** and writing frontend tests using **Jest, React Testing Library and Vitest**. Of all these technologies, the latest versions at the time of writing were used. The backend especially is somewhat littered with comments. This is because this is a project I would like to derive a lot from in the future and the comments help to clear out a number of problems I faced during the developement. For anyone else, though, I recommend not to bother reading them.

The game state is alive in the session on the backend, while the frontend allows the user to interact with it. While inferior to having the whole game run fully on the frontend and have the backend only serve the game board at the beginning, this is how the project started off in college and I chose not to change it, choosing instead to focus on the web aspect. 

### Built With

The **backend** is written in **Java, using Spring framework**. Communication with the **PostgreSQL database** is done using **Hibernate JPA**. **JUnit and Mockito** is used during testing. **Maven** is used as a project management tool and **IntelliJ IDEA** was used as an IDE.

The **frontend** project was created using **npm create vite, using TypeScript and React**. Custom implementation of _useFetch_ hook was later replaced by **React Query** and state management classes by **Redux Toolkit and Redux-persist** and these old files were left unused marked as _deprecated_. For testing on the frontend, **Vitest** is used due to the simplicity of setup on a TypeScript project. It effectively replaces **Jest** and **React Testing Library** was used to mount the react components during testing. A number of smaller libraries was used for some components like progress bars and skeleton loading. Visual Studio Code was used as an IDE.

## Features

**SPA** - The frontend is a **single page application**, it never refreshes after the first load of the page. A major feature of this project is that even during _login and logout_, the page never refreshes, which, from my observation, is rather unique and uncommon. The backend sends the main and only _index.html_ if the request's URL doesn't match anything else. 

**Session** - The backend is storing all user data, namely the _CSRF token_, _game board_ and _authorization state_ in a session. The frontend keeps track of this session in Redux and persists it with Redux-persist, so the state is maintained during manual refreshes of the page. 

**Skeleton loading** - While the frontend waits for API data requests from the backend it displays component placeholders, also known as _skeleton loading_. Careful attention was assigned to ensure that once the data loads, website layout does not jump(the skeleton loading takes the same shape, size and position as the fully loaded component). This is another major feature of this project, since, from my observation, even very highly popular websites do have some layout jumps once the data fully loads.

| ![Showcase of skeleton loading of the game board.](projectImages/skeletonLoading1.gif?raw=true) | 
|:--:| 
| *Showcase of skeleton loading of the game board* |

| ![Showcase of skeleton loading of leaderboard and comments.](projectImages/skeletonLoading2.gif?raw=true) | 
|:--:| 
| *Showcase of skeleton loading of leaderboard and comments* |

**Tests** - Both the backend and the frontend are tested with unit and integration tests. 

## Contact

[LinkedIn](https://linkedin.com/in/samuel-kačmár-381621270)

## How to build

- To build the backend project(this also builds the frontend project, so there is no need to separately build the frontend):
  1. Make sure you have [Maven](https://maven.apache.org/) and [Java JDK](https://openjdk.org/) installed on your machine. If you have installed [IntelliJ IDEA](https://www.jetbrains.com/idea/) as your IDE, these might have alreay been downloaded for you
  1. Open the project in terminal
  1. Run command `mvn package` (this automatically calls `mvn install` to download dependencies). After this, a JAR file should be created in target folder. Navigate to the folder and run it as so: `java -jar gamestudio-5780-1.0-SNAPSHOT.jar --spring.profiles.active=prod`. Setting the spring profile to prod disables CORS settings which allow CORS requests used during developement of the frontend(since during developement, frontend files are served by a node dev server and requests to the backend are cross-origin)
- To build the frontend project:
  1. Make sure you have [NodeJs](https://nodejs.org/) installed on your machine
  1. Open the [frontend project](frontend) in terminal
  1. Install project dependencies by running command: `npm install`
  1. Run command `npm run build`

There is no need for any database migrations, but the database must be running to run the backend server. You can change the database data in [application.properties](src/main/resources/application.properties).

## How to run during developement

Start the frontend dev server with `npm run dev`. Run the backend project with `mvn -DskipTests=true package` or just use the _run_ option in IDE.
