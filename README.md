# My AI Photography Assistant 📸

This is a full-stack application featuring an AI-powered chatbot designed to act as a personal assistant for my photography. This project serves as a deep dive into building production-grade AI applications, focusing on advanced backend engineering with Java, Spring AI, and modern cloud-native practices.

The project consists of a Java Spring Boot backend that serves the AI logic and a modern React frontend that provides a cute, energetic, and user-friendly chat interface.

**Live Demo:** [https://photo-ai-assitant.web.app](https://photo-ai-assitant.web.app)

---

## 🛠️ Technical Highlights

This project implements a wide range of advanced AI engineering concepts and technologies:

* **AI Development Framework:** Leverages a powerful combination of **Spring AI** and **LangChain4j** to build a robust and scalable AI backend.
* **Advanced RAG Implementation:** Implements a sophisticated **Retrieval-Augmented Generation (RAG)** pipeline. This includes practical application of RAG principles and advanced tuning techniques to ensure the AI pulls from a specialized knowledge base for factually grounded answers.
* **PgVector:** Utilizes **PostgreSQL with the `pgvector` extension** as a high-performance vector store, integrated with cloud database services for scalability and reliability.
* **Autonomous Tool Calling:** The AI agent is designed with **Tool Calling** capabilities, allowing it to autonomously invoke external tools, such as the Google Calendar API, to perform actions and retrieve real-time information.
* **Conversational Memory & State Management:** Implements core Spring AI features for managing **dialogue memory**, enabling natural, multi-turn conversations where context is preserved.
* **Prompt Engineering & Optimization:** Applies advanced **prompt engineering** techniques to optimize the AI's performance, ensuring accurate and relevant responses.
* **Structured Output:** Utilizes Spring AI's capabilities to generate **structured JSON output**, enabling reliable data exchange between the AI and other application components.
* **AI Agent Development:** Explores the principles behind autonomous AI agents for more proactive assistant capabilities.
* **Flexible Model Integration:** The architecture supports multiple methods for integrating with Large Language Models, including options for local deployment.
* **Serverless & AI as a Service:** The backend is designed for modern cloud-native deployment, containerized with Docker and deployed as a **serverless** service on Google Cloud Run, embodying the "AI as a Service" paradigm.
* **Exploration of Modern AI Concepts:** The project serves as a platform for exploring cutting-edge concepts like **multimodality, agentic workflows, and large model evaluation**.

---

## 🏛️ Project Architecture

The project follows a modern, scalable architecture that separates the frontend and backend concerns. The backend is built with Spring Boot and leverages Spring AI for seamless integration with Large Language Models. The frontend is a responsive React application.

![AI Photography Assistant Backend Architecture](https://i.imgur.com/rV3Fwqy.png)

---

## 💻 Tech Stack

| Category     | Technology                                                                                                                                                                                           |
| :----------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Backend** | `Java 21`, `Spring Boot 3`, `Spring AI`, `LangChain4j`, `RAG Pattern`, `PostgreSQL (with pgvector)`, `Google Calendar API`, `Maven`, `Docker`                                                                        |
| **Frontend** | `React`, `Tailwind CSS`, `EventSource API (SSE)`                                                                                                                                                     |
| **Deployment**| `Google Cloud Run`, `Google Cloud Build`, `Google Artifact Registry`, `Firebase Hosting`                                                                                                             |

![Technology Stack](https://i.imgur.com/9aR4j0V.png)

---

## 🚀 Quick Start

### Prerequisites

* Java 21
* Apache Maven
* Node.js (v18 or higher recommended)
* npm
* Google Cloud CLI (`gcloud`)
* Firebase CLI (`npm install -g firebase-tools`)

### Backend Setup

1.  **Clone the repository.**
2.  **Google Calendar API Setup:**
    * Create a Google Cloud Project.
    * Enable the Google Calendar API.
    * Create a Service Account and grant it the "Editor" role.
    * Create and download a JSON key for the service account. Rename it to `service-account.json` and place it in the `src/main/resources` directory.
    * Share your target Google Calendar with the service account's email address, giving it "Make changes to events" permissions.
3.  **Configuration:**
    * Create an `application-local.yml` file in `src/main/resources`.
    * Add your Google Calendar ID and OpenAI API Key:
        ```yaml
        spring:
          ai:
            openai:
              api-key: "YOUR_OPENAI_API_KEY"
        google:
          calendar:
            id: "your-calendar-id@group.calendar.google.com"
        ```
4.  **Run the application:**
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
    ```
    The backend will be available at `http://localhost:8080`.

### Frontend Setup

1.  **Navigate to the `frontend` project directory.**
2.  **Install dependencies:**
    ```bash
    npm install
    ```
3.  **Configure API Endpoint:**
    * Open `src/App.jsx`.
    * Ensure `API_BASE_URL` points to your local backend:
        ```javascript
        const API_BASE_URL = 'http://localhost:8080/api'; // Or your backend port
        ```
4.  **Run the application:**
    ```bash
    npm start
    ```
    The frontend will be available at `http://localhost:3000`.

---
