# Smart Parking Application

The **Smart Parking Application** is designed to streamline parking space management, integrate payment systems, and track parking spot availability. This application is built using **Spring Boot**, **MySQL**, and **Docker Compose**.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Endpoints](#endpoints)
- [Testing the Application](#testing-the-application)
- [Docker Compose Setup](#docker-compose-setup)
- [Reference Documentation](#reference-documentation)
- [License](#license)

---

## Getting Started

To get started with the Smart Parking Application, follow these instructions to set up the environment and run the application.

### Prerequisites

Before running the project, make sure you have the following installed:

- **Docker** and **Docker Compose** for containerization.
- **JDK 11 or above** for building and running the Spring Boot application (if not using Docker).
- **Maven** (optional, for local builds if Docker isn't used).

### Installation

1. Clone the repository to your local machine:

    ```bash
    git clone https://github.com/your-username/smart-parking-app.git
    cd smart-parking-app
    ```

2. Ensure Docker is installed and running on your machine. If you don't have Docker installed, follow [Docker's installation guide](https://docs.docker.com/get-docker/).

---

## Technologies

The Smart Parking Application is built with the following technologies:

- **Spring Boot** - For backend development and RESTful API services.
- **MySQL** - Database for parking records, transactions, and user data.
- **Docker** - Containerization for deployment.
- **Docker Compose** - For managing multi-container Docker applications.
- **Stripe** - Payment processing for parking payments.
- **MPESA Payment Integration** - Payment processing for parking payments.
- **Swagger** - API documentation and testing interface.
- **Spring Data JPA** - ORM for MySQL database access.
- **React js** - For frontend development

---

## Setup Instructions

- run command docker-compose up
- The project will start on port 8080
- access the project on http:localhost:8080

In your project directory, make sure you have the `docker-compose.yml` file. Here’s an example configuration:

