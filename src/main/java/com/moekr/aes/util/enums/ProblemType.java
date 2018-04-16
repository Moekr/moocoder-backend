package com.moekr.aes.util.enums;

public enum ProblemType {
	JAVA("Java") {
		@Override
		public boolean isCoverage() {
			return false;
		}

		@Override
		public String initialCommand() {
			return "mvn -fn clean test clean -Dmaven.repo.local=/var/ws/repository/\n";
		}

		@Override
		public String runScript(String problemName) {
			return "pushd /var/ws/code/" + problemName + "/\n"
					+ "mvn -fn clean test -Dmaven.repo.local=/var/ws/repository/\n"
					+ "popd\n"
					+ "mkdir -p ./test-reports/" + problemName + "/\n"
					+ "cp /var/ws/code/" + problemName + "/target/surefire-reports/*.xml ./test-reports/" + problemName + "/\n";
		}

		@Override
		public FileType fileType(String filePath) {
			if (filePath.startsWith("/src/main/")) {
				return FileType.PUBLIC;
			} else {
				return FileType.PROTECTED;
			}
		}
	}, PYTHON("Python") {
		@Override
		public boolean isCoverage() {
			return false;
		}

		@Override
		public String initialCommand() {
			return "pip3 install -r requirements.txt";
		}

		@Override
		public String runScript(String problemName) {
			return "pushd /var/ws/code/" + problemName + "/\n"
					+ "nosetests3 --with-xunit || :\n"
					+ "popd\n"
					+ "mkdir -p ./test-reports/" + problemName + "/\n"
					+ "cp /var/ws/code/" + problemName + "/nosetests.xml ./test-reports/" + problemName + "/\n";
		}

		@Override
		public FileType fileType(String filePath) {
			if (filePath.startsWith("/src/")) {
				return FileType.PUBLIC;
			} else {
				return FileType.PROTECTED;
			}
		}
	}, JAVA_COVERAGE("Java Coverage") {
		@Override
		public boolean isCoverage() {
			return true;
		}

		@Override
		public String initialCommand() {
			return "mvn -fn clean cobertura:cobertura clean -Dmaven.repo.local=/var/ws/repository/\n";
		}

		@Override
		public String runScript(String problemName) {
			return "pushd /var/ws/code/" + problemName + "/\n"
					+ "mvn -fn clean cobertura:cobertura -Dmaven.repo.local=/var/ws/repository/\n"
					+ "popd\n"
					+ "mkdir -p ./coverage-reports/" + problemName + "/\n"
					+ "cp /var/ws/code/" + problemName + "/target/site/cobertura/*.xml ./coverage-reports/" + problemName + "/\n";
		}

		@Override
		public FileType fileType(String filePath) {
			if (filePath.startsWith("/src/test/")) {
				return FileType.PUBLIC;
			} else {
				return FileType.PROTECTED;
			}
		}
	}, PYTHON_COVERAGE("Python Coverage") {
		@Override
		public boolean isCoverage() {
			return true;
		}

		@Override
		public String initialCommand() {
			return "pip3 install coverage && pip3 install -r requirements.txt";
		}

		@Override
		public String runScript(String problemName) {
			return "pushd /var/ws/code/" + problemName + "/\n"
					+ "nosetests3 --with-coverage --cover-xml || :\n"
					+ "popd\n"
					+ "mkdir -p ./coverage-reports/" + problemName + "/\n"
					+ "cp /var/ws/code/" + problemName + "/coverage.xml ./coverage-reports/" + problemName + "/\n";
		}

		@Override
		public FileType fileType(String filePath) {
			if (filePath.startsWith("/test/")) {
				return FileType.PUBLIC;
			} else {
				return FileType.PROTECTED;
			}
		}
	};

	private final String name;

	ProblemType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract boolean isCoverage();

	public abstract String initialCommand();

	public abstract String runScript(String problemName);

	public abstract FileType fileType(String filePath);
}
