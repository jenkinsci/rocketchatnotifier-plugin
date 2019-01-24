# Rocket Chat Notification Plugin for Jenkins

[![Build Status](https://travis-ci.org/jenkinsci/rocketchatnotifier-plugin.svg?branch=master)](https://travis-ci.org/jenkinsci/rocketchatnotifier-plugin) 
[![Build Status](https://ci.jenkins.io/job/Plugins/job/rocketchatnotifier-plugin/job/master/badge/icon)](https://ci.jenkins.io/blue/organizations/jenkins/Plugins%2Frocketchatnotifier-plugin/branches/)
[![Build Status](https://jenkins.martinreinhardt-online.de/buildStatus/icon?job=OSS/rocketchatnotifier-plugin/master)](https://jenkins.martinreinhardt-online.de/blue/organizations/jenkins/OSS%2Frocketchatnotifier-plugin/branches/)
[![codecov](https://codecov.io/gh/jenkinsci/rocketchatnotifier-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jenkinsci/rocketchatnotifier-plugin)
[![Known Vulnerabilities](https://snyk.io/test/github/jenkinsci/rocketchatnotifier-plugin/badge.svg)](https://snyk.io/test/github/jenkinsci/rocketchatnotifier-plugin)
[![](https://img.shields.io/badge/style-issues-yellow.svg?style=flat&label=JIRA)](https://issues.jenkins-ci.org/browse/JENKINS-48905?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened%2C%20%22In%20Review%22)%20AND%20component%20%3D%20rocket-chat-notifier-plugin)

The latest stable version is available at Plugins Center. Dev builds are available via the [Experimental Plugins Update Center](https://jenkins.io/blog/2013/09/23/experimental-plugins-update-center/).

<a name="donation"></a>
> Feel free to **donate**
>
> <a target="_blank" href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=D88ZDNH6AANPJ">
> <img alt="" border="0" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif"/>
> </img></a>
> Or donate Bitcoins: bitcoin:3NKtxw1SRYgess5ev4Ri54GekoAgkR213D
>
> [![Bitcoin](https://martinreinhardt-online.de/bitcoin.png)](bitcoin:3NKtxw1SRYgess5ev4Ri54GekoAgkR213D)
>
> Also via [greenaddress](https://greenaddress.it/pay/GA3ZPfh7As3Gc2oP6pQ1njxMij88u/)

## Usage

### Pipeline

You can use it in the Workflow/Pipeline DSL
```
node {
    try {
     ...
    } catch (e) {
        rocketSend channel: 'abc', message: 'test'
        throw e
    }
}
```
If you omit channel you can shorten it as it would now use the global default channel:
```
node {
    try {
     ...
    } catch (e) {
        rocketSend 'test'
        throw e
    }
}
```

The message looks then like this:

![sample message](rocket_sample_message.png)

It also works with normal jobs:


![job config](rocket_job_config.png)

## Admin settings

You can define a default notification channel:


![sample message](rocket_admin_settings.png)

# Contribution

## Bugs

If you find a bug in the source code or a mistake in the documentation, you can help us by
submitting an issue to our [JIRA](https://issues.jenkins-ci.org/browse/JENKINS-39690?jql=project%20%3D%20JENKINS%20AND%20component%20%3D%20rocket-chat-notifier-plugin). Even better you can submit a Pull Request
with a fix.

First search if the issue is already described!

If not create a new issue:

* Tell about your environment:
  * operating system and version
  * Jenkins version
  * Java version
  * RocketChat version
* Describe your issue
  * describe your steps leading to the issue
  * attach error logs or screenshots
  * if possible provide test case or screenshots
