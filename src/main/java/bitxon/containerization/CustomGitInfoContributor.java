package bitxon.containerization;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomGitInfoContributor implements InfoContributor {

    private final GitProperties gitProperties;

    public CustomGitInfoContributor(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        var gitInfo = Map.of(
            "commitId", gitProperties.getCommitId(),
            "branch", gitProperties.getBranch()
        );
        builder.withDetail("customGitInfo", gitInfo);
    }
}
