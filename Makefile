clean:
	find src -name "*.class" -delete

run-perso-compile:
	cd src && javac Main.java

run-perso-exec:
	cd src && java Main "$(params)"

watch_command:clean run-perso-compile
	make run-perso-exec params=$(param)

verif:
	watch make watch_command param=montest
