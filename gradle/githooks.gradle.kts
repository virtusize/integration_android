tasks.register<Copy>("installGitHooks") {
    from(File(rootProject.rootDir, ".githooks/pre-push"))
    into(File(rootProject.rootDir, ".git/hooks"))
    fileMode = "0775".toInt(8) // Octal representation of file mode
}

// Uncomment the following line if you want the installGitHooks task to be run automatically
// afterEvaluate {
//    tasks.getByPath(":virtusize:preBuild").dependsOn(tasks.named("installGitHooks"))
// }
