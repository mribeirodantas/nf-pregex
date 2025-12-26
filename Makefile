.PHONY: assemble clean install test

assemble:
	./gradlew assemble

clean:
	./gradlew clean

install:
	./gradlew installPlugin

test:
	./gradlew test

check:
	./gradlew check
