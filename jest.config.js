module.exports = {
  testEnvironment: 'node',
  roots: ['<rootDir>/infrastructure/test'],
  testMatch: ['**/*.test.ts'],
  transform: {
    '^.+\\.tsx?$': 'ts-jest'
  },
  modulePaths: [
    "<rootDir>"
  ],
  moduleDirectories: [
    "node_modules"
  ],
  moduleNameMapper: { 
  }, 
  reporters: [
    'default',
    [ 'jest-junit', {
      outputDirectory: "report",
      outputName: "jestReporFile.xml",
    } ]
  ]
};
