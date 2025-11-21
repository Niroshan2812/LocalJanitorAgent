# The Local File System "Janitor" Agent

 ### Internal structure and information movement in agent
 
<img width="733" height="368" alt="image" src="https://github.com/user-attachments/assets/e453db3c-878b-4f54-8775-0586e6ec695b" />



## A Local AI Agent that organizes your files.

* most AI agents live in the cloud and can only talk. The Janitor Agent has "hands." It runs locally on your machine, analyzes your file system, and uses a remote LLM (In here hosted on Google Colab Or you can install ollama in local machine and run) to make intelligent decisions about how to organize, move, and clean up your files.

Unlike cloud-based agents, this runs entirely **offline** for privacy and speed. (If you choose run ollama in Local machine)

## In here 

* Privacy First So Uses can **Ollama** to run the AI brain locally. No file data leaves your machine.
* Built-in `SafetyService` restricts file operations to a specific `Janitor_Sandbox` folder to prevent accidental data loss.
* Uses vectorized thought processes (JSON Arrays) to execute multiple file operations in a single inference pass, significantly reducing latency.
* Built on a modular `AgentTool` interface, making it easy to add new capabilities.

##  Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **AI Engine:** Ollama (Running Llama3, Mistral, or Qwen)
* **Communication:** REST Client
* **JSON Processing:** Jackson (with Batch Array Mapping)

### Since we are offloading the heavy AI processing, I USE google Colab for run ollama 

#### Set up "The Brain" (Google Colab)

* Open a new Google Colab Notebook.
* Change Runtime to T4 GPU.
* You need to create an Ngrok account -> gO -> Endpoints and Traffic Policy ->  create Endpoint -> Agent Endpoints -> Copy Key
* Then include it in "Secrets" (Google colab -> Secrets ) to storing environment variables, then provide notebok access. 
* Use "OllamaModel/fixed_ModelConfig.ipynb" script to install ollama and Ngrok
* Then it will give URL like this -> "https://xyz.ngrok-free.app"

#### Set up Spring boot 

* Clone repo
* Open src/main/resources/application.properties
* Update the agent.brain.url with the Ngrok URL you got from Step 1.
 ``
application.properties
agent.brain.url=https://your-ngrok-url.ngrok-free.app/api/chat
agent.model=qwen2.5-coder
``
##### * Note ->  Crucial Safety step ==> Open SafetyService.java and configure your Sandbox path. Do not set this to your root C:/ drive.
----------------------------------------------------------------------------------------------------------------

#### Run the Agent

* Build the project
  ``
  mvn clean install
  ``
* Run the application

``
mvn spring-boot:run
``
* Watch the logs !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

--------------------------------------------------------------------------------------------------------------------

## Future Updates 

* Automatically extract .zip files into organized folders.
* Remove junk files.
* Allow the agent to run continuously in the background, watching for new files.
* Use a Multimodal model, like LLaVA So, it can look inside images and rename them based on content. for example IMG11.JPG -> Brother_In_park.jpg


----------------------------------------------------------END-----------------------------------------------------

