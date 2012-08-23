package org.jenkinsci.plugins.repo_updatesite;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleTest {

    @Test
    public void test() {
        String gav = "org.jenkins-ci.plugins:gravatar:1.1";
        if (!gav.contains(":hpi:")) {
            // org.jenkins-ci.plugins:gravatar:jar:1.1
            final String[] cords = gav.split(":");
            System.out.println(cords.length);
            gav = "";
            for (int i = 0; i < cords.length; i++) {
                String c = cords[i];
                if (i == 2) {
                    gav += ":hpi";
                }
                if(i != 0){
                    gav += ":";
                }
                gav += c;
            }
        }
        System.out.println(gav);
    }

}
