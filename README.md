<p align="center">
  <img src="static/header.png" />
</p>

# I'm truly Non-AI-Powered JFR Analyzer

### Where to find more information about me

https://www.jeffrey-analyst.cafe/

### What I am capable of

I'm an expert in visualizing JFR events using various types of graphs. My purpose is to help you to profile 
your application and find any spots in your code that can be improved to make it faster or consume fewer resources. 

### How to get me running
 
- Frontend (Vue.Js - Node 22)
- Backend (Java 22 - Jeffrey will always be on the cutting-edge version!), Java server serves the frontend part 

#### Using Prepared Builds

Download the latest [jeffrey.jar](https://github.com/petrbouda/jeffrey/releases/latest/download/jeffrey.jar)

Check the latest version:
```
java -jar jeffrey.jar version
```

Start with:
```
java -jar jeffrey.jar
```

- Open in the browser: http://localhost:8585

#### Using Docker Images

Start an empty container (JFR Profiles for testing purposes - https://github.com/petrbouda/jeffrey-recordings):
- Newly added profiles will be stored in ~/.jeffrey/recordings
```
docker run -it --network host -v $HOME:/root petrbouda/jeffrey
```

Start a container with testing JFR Profiles:
- There is no volume, newly added profiles will be lost after removing the container
```
docker run -it --network host petrbouda/jeffrey-examples
```

- Open in the browser: http://localhost:8585

#### Build me from sources

- You need to have Node.js installed (Maven plugin uses `npm` and run `vite build`)

```
git clone https://github.com/petrbouda/jeffrey.git && cd jeffrey
mvn clean package
cd build/target
```

```
java -jar jeffrey.jar
```

- Open in the browser: http://localhost:8585

### Build using a Docker Container

Docker Image is available on DockerHub, or you can build your own ([Dockerfile](docker/Dockerfile-builder))

```
git clone https://github.com/petrbouda/jeffrey.git && cd jeffrey
docker run -it -v "$PWD":/app petrbouda/jeffrey-builder mvn clean package -f /app/pom.xml
cd build/target
```

```
java -jar jeffrey.jar
```

### Running for development

- Start the Backend in IDE: `service/core/src/main/java/pbouda/jeffrey/Application.java`
- Start UI

```
cd pages
npm run dev
```

- Open in the browser: http://localhost:5173

### Who inspired me

Big thanks to these projects and the developers working on them:
- https://github.com/async-profiler/async-profiler
- https://github.com/openjdk/jmc
- https://github.com/Netflix/flamescope
- https://apexcharts.com/
