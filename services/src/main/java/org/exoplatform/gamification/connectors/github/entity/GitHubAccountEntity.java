package org.exoplatform.gamification.connectors.github.entity;


import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;

import lombok.Data;

import java.io.Serializable;

@Entity(name = "GitHubAccountEntity")
@ExoEntity
@Table(name = "GITHUB_ACCOUNT")
@Data
@NamedQueries({
        @NamedQuery(
                name = "GitHubAccountEntity.getAccountByGithubId",
                query = "SELECT account FROM GitHubAccountEntity account where account.gitHubId = :gitHubId "
        ),
        @NamedQuery(
                name = "GitHubAccountEntity.getAccountByUserName",
                query = "SELECT account FROM GitHubAccountEntity account where account.userName = :userName "
        )

                })
public class GitHubAccountEntity implements Serializable {


    @Id
    @SequenceGenerator(name="SEQ_GITHUB_ACCOUNT_ID", sequenceName="SEQ_GITHUB_ACCOUNT_ID")
    @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_GITHUB_ACCOUNT_ID")
    @Column(name = "ID")
    protected Long id;

    @Column(name = "GITHUB_ID", unique = true, nullable = false)
    protected String gitHubId;

    @Column(name = "USER_NAME", unique = true, nullable = false)
    protected String userName;
}
