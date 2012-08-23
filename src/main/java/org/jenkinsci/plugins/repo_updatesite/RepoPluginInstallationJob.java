package org.jenkinsci.plugins.repo_updatesite;

import hudson.PluginWrapper;
import hudson.model.Messages;
import hudson.model.UpdateSite;
import hudson.model.UpdateSite.Plugin;
import hudson.security.ACL;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import jenkins.RestartRequiredException;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.jenkinsci.plugins.repo_updatesite.aether.Aether;
import org.jenkinsci.plugins.repo_updatesite.aether.AetherConfig;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class RepoPluginInstallationJob extends hudson.model.UpdateCenter.InstallationJob {
    private static final Logger LOGGER = Logger.getLogger(RepoPluginInstallationJob.class.getName());

    private final AetherConfig config;

    public RepoPluginInstallationJob(AetherConfig config, Plugin plugin, UpdateSite site, Authentication auth, boolean dynamicLoad) {
        super(plugin, site, auth, dynamicLoad);
        this.config = config;
    }

    @Override
    public void _run() throws IOException, InstallationStatus {

        try {
            final File downloadedPlugin = this.downloadPlugin(plugin);
            this.replace(getDestination(), downloadedPlugin);
        } catch (ArtifactResolutionException e1) {
            throw new Failure(e1);
        }

        // if this is a bundled plugin, make sure it won't get overwritten
        PluginWrapper pw = plugin.getInstalled();
        if (pw != null && pw.isBundled()) {
            SecurityContext oldContext = ACL.impersonate(ACL.SYSTEM);
            try {
                pw.doPin();
            } finally {
                SecurityContextHolder.setContext(oldContext);
            }
        }

        if (dynamicLoad) {
            try {
                pm.dynamicLoad(getDestination());
            } catch (RestartRequiredException e) {
                throw new SuccessButRequiresRestart(e.message);
            } catch (Exception e) {
                throw new IOException2("Failed to dynamically deploy this plugin", e);
            }
        } else {
            throw new SuccessButRequiresRestart(Messages._UpdateCenter_DownloadButNotActivated());
        }

    }

    private File downloadPlugin(Plugin plugin) throws ArtifactResolutionException {

        RepositorySystem repoSystem = Aether.newRepositorySystem();
        RepositorySystemSession session = Aether.newSession(repoSystem, new File(getDestination().getParentFile(), ".temp-plugin-repo"));
        RemoteRepository repo = new RemoteRepository("central", "default", config.url);

        LOGGER.info("get '" + plugin.gav + "' from " + config.url);

        final String[] cords = plugin.gav.split(":");
        Artifact artifact = null;
        if (cords.length == 3) {
            artifact = new DefaultArtifact(cords[0], cords[1], "hpi", cords[2]);
        } else {
            artifact = new DefaultArtifact(cords[0], cords[1], cords[2], cords[3]);
        }

        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.addRepository(repo);

        ArtifactResult artifactResult = repoSystem.resolveArtifact(session, artifactRequest);
        artifact = artifactResult.getArtifact();
        return artifact.getFile();
    }
}
