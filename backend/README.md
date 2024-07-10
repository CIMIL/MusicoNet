# Musico App Development

## Project Description

Musico is a social network app designed for musicians. It allows users to create profiles, connect with other musicians based on location and musical interests, form groups, create or join events, and upload music clips. The app features a real-time mood analysis tool to suggest matches based on the mood of the music being played. The goal is to facilitate collaboration and networking among musicians.

## Instructions

To run the backend server, navigate to the `backend` directory and run the following commands:

### Pre-requisites

The backend is developed using the following technologies, make sure you have them installed on your machine:

- Java 17
- Python 3.11
- Gradle 8.7
- Docker && Docker Compose

### Steps to run the backend

1. Clone the repository:

    ```bash
    git clone https://github.com/CIMIL/MusicoNet.git
    cd backend
    ```

2. Run the docker-compose file:

    ```bash
    docker-compose up
    ```

3. Launch the microservices:
   - For each microservice, navigate to the respective directory and run the following command:

    ```bash
    gradle bootRun
    ```

    - For the `essentia` microservice, follow these steps:

    ```bash
    cd essentia/essentia-service
    python -m venv env
    source env/bin/activate
    pip install -r requirements.txt
    python3 main.py
    ```

### Additional Information

Make sure to check address and port of the services in the `application.properties` file of each microservice and on the `docker-compose.yml` file.

### Support

If you need help, please write an email to [jacopo.tomelleri@studenti.unitn.it](mailto:jacopo.tomelleri@studenti.unitn.it)
