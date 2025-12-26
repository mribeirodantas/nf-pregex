.PHONY: assemble clean install test

assemble:
	./gradlew assemble

clean:
	./gradlew clean

install:
	./gradlew copyPluginZip

test:
	./gradlew test

check:
	./gradlew check
