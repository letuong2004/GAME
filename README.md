## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## Run the Caro Game Server and Client

1. Compile:
   - `javac -d bin src\\game\\model\\Account.java src\\game\\model\\Board.java src\\game\\model\\Room.java src\\game\\server\\Server.java src\\game\\server\\ClientHandler.java src\\game\\client\\Client.java`
2. Start the server:
   - `java -cp bin game.server.Server`
3. Start a client:
   - `java -cp bin game.client.Client`
   - Or start the GUI client:
   - `java -cp bin App`

## Client Commands

- `/register username password nickname avatar`
- `/login username password`
- `/chat message`
- `/create-room roomName [password]`
- `/list-rooms`
- `/join-room roomId [password]`
- `/quick-play`
- `/bot`
- `/move row col`
- `/forfeit`
- `/draw`
- `/leave`
- `/status`
- `/help`
