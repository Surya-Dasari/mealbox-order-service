withVault([
  vaultSecrets: [[
    path: 'secret/mealbox/ci',
    secretValues: [
      [envVar: 'NEXUS_USER', vaultKey: 'nexus_username'],
      [envVar: 'NEXUS_PASS', vaultKey: 'nexus_password']
    ]
  ]]
]) {
  sh '''
cat > settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>mealbox-maven-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
EOF

mvn deploy -DskipTests -s settings.xml
'''
}

