import jenkins.model.Jenkins

def configSimpleTheme(def descriptor, String cssUrl, String jsUrl) {
    descriptor.cssUrl = cssUrl.trim()
    descriptor.jsUrl = jsUrl.trim()
    println "--> setting theme attributes... done"
    descriptor.save()
}

def themeDecorator = Jenkins.getInstance().getDescriptor("org.codefirst.SimpleThemeDecorator")
configSimpleTheme(themeDecorator, "/userContent/custom.css", "")
