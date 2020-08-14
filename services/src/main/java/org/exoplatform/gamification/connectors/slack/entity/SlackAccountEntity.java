package org.exoplatform.gamification.connectors.slack.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.Data;

@Entity(name = "SlackAccountEntity")
@ExoEntity
@Table(name = "GAM_SLACK_ACCOUNTS")
@Data
@NamedQueries({
    @NamedQuery(name = "SlackAccountEntity.getAccountBySlackId", query = "SELECT account FROM SlackAccountEntity account where account.slackId = :slackId "),
    @NamedQuery(name = "SlackAccountEntity.getAccountByIdentityId", query = "SELECT account FROM SlackAccountEntity account where account.identityId = :identityId "),
    @NamedQuery(name = "SlackAccountEntity.getAccountByEmail", query = "SELECT account FROM SlackAccountEntity account where account.email = :email "),
    @NamedQuery(name = "SlackAccountEntity.getAccountByToken", query = "SELECT account FROM SlackAccountEntity account where account.token = :token ")
})
public class SlackAccountEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_GAM_SLACK_ACCOUNTS_ID", sequenceName = "SEQ_GAM_SLACK_ACCOUNTS_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GAM_SLACK_ACCOUNTS_ID")
  @Column(name = "ID")
  protected Long   id;

  @Column(name = "SLACK_ID", unique = true, nullable = false)
  protected String slackId;

  @Column(name = "SLACK_TEAM_ID", unique = true, nullable = false)
  protected String slackTeamId;

  @Column(name = "IDENTITY_ID", unique = true, nullable = false)
  protected Long identityId;

  @Column(name = "EMAIL", unique = false, nullable = false)
  protected String email;

  @Column(name = "TOKEN", unique = true, nullable = false)
  protected String token;
}
