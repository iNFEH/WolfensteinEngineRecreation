# Wolfenstein Engine Recreation

A Java Swing based project that recreates a 2.5D rendering engine in the style of 
Wolfenstein 3D. It renders textured walls from a two-dimensional map and supports
first-person and top-down views.

This is a partial engine recreation, not a complete game nor an official 
Wolfenstein release. It does not require the original game to run. Also this project
is made without the use of AI as an exercise in programming.

> [!NOTE]
> **Explore the implementation:** [How does it work?](docs/how-does-it-work.md)
> explains the mechanics behind the raycasting and more.
> [Architecture](docs/architecture.md) describes the design and includes a UML diagram.

## Preview

![First-person view of the engine](docs/images/demo.gif)

## Requirements

For running:
- JDK 21 or newer.

For development:
- Maven 3.9 or newer.

The dev container includes JDK25, Maven and X11 libraries for display forwarding.

## Setup and run

If you only intend to run it you can skip maven entirely and compile and run using (from the project root):
```sh
javac -d bin src/main/java/*.java
```
Linux:
```sh
java -cp bin:src/main/resources Control
```
Windows:
```sh
java -cp "bin;src/main/resources" Control
```

For development I instead recommended to compile and run the PMD and Checkstyle checks:

```sh
mvn clean verify
```

Start the application using:

```sh
mvn compile exec:exec
```

Maven copies the textures from `src/main/resources/` into its build output and
includes them in the JAR. 

### Display access from the container

The configuration in `.devcontainer/devcontainer.json` forwards the host's `DISPLAY`
and mounts `/tmp/.X11-unix` (A Wayland desktop must provide XWayland for this setup.)
You must also allow the container user to connect to the host's X11 display.

1. In a terminal inside the container, run `id -u` to check the container user's UID.
2. Install `xhost` on the **host**.
3. In a terminal in your **host's graphical desktop session**, run the following
   command. This example assumes a container UID of `1000`:

   ```sh
   xhost "+si:localuser:#1000"
   ```

4. Run `mvn compile exec:exec` inside the container.

Replace `1000` if needed. Only grant display access to code you trust! X11 access exposes 
more than the game window. To remove this permission later, run on the host:

```sh
xhost "-si:localuser:#1000"
```

## Usage

The game window captures the mouse, Press **Escape** to release capture and restore the 
pointer.

| Input | First-person view | Top-down view |
| --- | --- | --- |
| `W` / `S` | Move forward / backward | Move up / down on the map |
| `A` / `D` | Strafe left / right | Move left / right on the map |
| Left / right arrow | Turn left / right | Turn left / right |
| Mouse input | Turn along the horizontal axis | Turn the player's aim direction |
| `Escape` | Releases the mouse | Releases the mouse |
| `F10` | Open or close options | Open or close options |
| `F11` | Switch to top-down view | Switch to first-person view |

The options panel allows the user to set the mouse sensitivity separate for the two modes
and a to set the field-of-view for the first-person view. Press `F10` or `Escape` in the
dialog to hide it. Settings are not stored between sessions currently.

## Known issues and limitations

- Mouse input can feel inconsistent when running through Docker and
  X11. The mouse input is also affected by desktop acceleration.
- The window has a fixed size. Change `VIEW_WIDTH` and `VIEW_HEIGHT` in
  `src/main/java/RenderSettings.java`, then recompile and restart to change 
  window resolution.

## Development

See [Architecture](docs/architecture.md) for the UML design diagram, class
responsibilities and runtime flow. See [How does it work?](docs/how-does-it-work.md)
for an explanation of the rendering mechanics and input handling.

Run `mvn clean verify` after Java or Maven configuration changes. PMD and Checkstyle
use `pmd-ruleset.xml` and `checkstyle.xml`. There is currently no test suite present.

Source code is in `src/main/java/`, textures are in `src/main/resources/tex/` and
generated files are put in `target/`. The map can be altered by editing the map table 
in `Map.java`. Cell types `0`, `1`, `2` and `3` mean empty, gray stone, red brick and 
wood. Nonzero cell types offer wall collision.

### Development container

The dev container is defined in `.devcontainer/`. Open the repository with a 
dev-container-capable IDE. The container comes with everything required to both run and 
develop the project.

The supplied configuration mounts a host SSH agent and the X11 display socket.
**Review those mounts before use!** These mounts are not application dependencies
and ensure `SSH_AUTH_SOCK` points to a running host SSH agent.

## License

Wolfenstein Engine Recreation source code is available under the [MIT License](LICENSE).
