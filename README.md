The "project-microservices" folder serves as the backend. To run this:

open terminal in 'usual' folder:
cd usual
mvn clean install
mvn srping-boot:run

open another terminal:
cd producer
mvn srping-boot:run

open another terminal:
cd consumer
mvn srping-boot:run

Then just open the other project that serves as the front end.

Open 2 emulators at once, turn them on, then select both the emulators upon running the project.
