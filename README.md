# MusicoNet

MusicoNet is a social network designed for musicians, leveraging the Internet of Musical Things paradigm. This repository contains the source code for the MusicoNet platform, which is fully detailed in the accompanying article, "MusicoNet: A Social Network for Musicians Based on the Internet of Musical Things Paradigm".

## Repository structure

This repository is organized into two main directories: `backend` and `frontend`.

### Backend

The `backend` directory contains the source code for all the microservices that constitute the MusicoNet platform. Every microservice has its own directory.

Additionally, this directory includes a `docker-compose` file to facilitate the creation of containers with all necessary project dependencies, such as the database and the message broker.

### Frontend

The `frontend` directory houses the source code for the mobile application, enabling users to interact with the MusicoNet platform.

For more information about the frontend code and how to run the mobile application, please refer to the [README](./frontend/README.md) file located in the `frontend` directory.

### Built with

Backend:

- [Spring](https://spring.io/)
- [Apache Kafka](https://kafka.apache.org/)
- [MongoDB](https://www.mongodb.com/)
- [GraphDB](https://www.ontotext.com/products/graphdb/)
- [MySQL](https://www.mysql.com/)
- [Essentia](https://essentia.upf.edu/)
- [Keycloak](https://www.keycloak.org/)
- [Docker](https://www.docker.com/)

Frontend:

- [React Native](https://reactnative.dev/)
- [Expo](https://expo.io/)
