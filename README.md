### Before starting:
1) Create an .env file in the project root with the following variables:
   POSTGRES_USER=
   POSTGRES_PASSWORD=
   POSTGRES_DB=task-tracker
   POSTGRES_URL=jdbc:postgresql://db:5432/task-tracker

The POSTGRES_USER and POSTGRES_PASSWORD variables can be your own. 
These variables will be used in the DB created after you run "docker-compose up"
2) Run your docker desktop app.
The installation URL 
- for windows: https://docs.docker.com/desktop/setup/install/windows-install/
- for macOS: https://docs.docker.com/desktop/setup/install/mac-install/
3) In the terminal run the command (replace <your-login-on-GitHub> with your login): 
>echo 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 | docker login ghcr.io -u <your-login-on-GitHub> --password-stdin 
4) From the project root, type "docker-compose up". 
This will create two docker containers: one for the task-tracker application and one for the task-tracker DB.
5) That's it. You can now run HTTP queries to the task-tracker app and SQL-queries to the task-tracker DB.


The task-tracker Github repo: https://github.com/a1be1/task-tracker
