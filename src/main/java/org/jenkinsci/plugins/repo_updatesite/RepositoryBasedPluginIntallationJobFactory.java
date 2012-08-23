package org.jenkinsci.plugins.repo_updatesite;

import hudson.Extension;
import hudson.model.UpdateCenter.UpdateCenterJob;
import hudson.model.UpdateSite;
import hudson.model.UpdateSite.Plugin;
import hudson.model.jobfactory.PluginIntallationJobFactory;
import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;
import org.jenkinsci.plugins.repo_updatesite.aether.AetherConfig;
import org.kohsuke.stapler.DataBoundConstructor;

public class RepositoryBasedPluginIntallationJobFactory extends PluginIntallationJobFactory {

    private final String repoUrl;

    @DataBoundConstructor
    public RepositoryBasedPluginIntallationJobFactory(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    @Override
    public UpdateCenterJob createPluginInstallJob(Plugin plugin, UpdateSite updateSite, Authentication authentication, boolean dynamicLoad) {
        AetherConfig config = new AetherConfig(repoUrl);
        return new RepoPluginInstallationJob(config, plugin, updateSite, authentication, dynamicLoad);
    }

    @Override
    public DefaultPluginIntallationJobFactoryDescriptor getDescriptor() {
        return (DefaultPluginIntallationJobFactoryDescriptor) Jenkins.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static class DefaultPluginIntallationJobFactoryDescriptor extends PluginIntallationJobFactoryDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.descriptor_displayName();
        }

    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + repoUrl;
    }

}
