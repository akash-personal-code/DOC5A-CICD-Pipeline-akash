package main

# Conftest policy for rendered Kubernetes YAML.
# Blocks common regulated-bank deployment risks:
# - mutable latest images
# - missing resource requests/limits
# - containers running as root
# - privileged containers
# - plaintext secret env values

is_workload {
  input.kind == "Deployment"
}

containers[c] {
  is_workload
  c := input.spec.template.spec.containers[_]
}

deny[msg] {
  containers[c]
  endswith(c.image, ":latest")
  msg := sprintf("container %q must not use the latest tag", [c.name])
}

deny[msg] {
  containers[c]
  not c.resources.requests.cpu
  msg := sprintf("container %q must set CPU request", [c.name])
}

deny[msg] {
  containers[c]
  not c.resources.requests.memory
  msg := sprintf("container %q must set memory request", [c.name])
}

deny[msg] {
  containers[c]
  not c.resources.limits.cpu
  msg := sprintf("container %q must set CPU limit", [c.name])
}

deny[msg] {
  containers[c]
  not c.resources.limits.memory
  msg := sprintf("container %q must set memory limit", [c.name])
}

deny[msg] {
  containers[c]
  not c.securityContext.runAsNonRoot
  msg := sprintf("container %q must run as non-root", [c.name])
}

deny[msg] {
  containers[c]
  c.securityContext.privileged == true
  msg := sprintf("container %q must not be privileged", [c.name])
}

deny[msg] {
  containers[c]
  env := c.env[_]
  lower(env.name) == "db_password"
  env.value
  msg := "DB_PASSWORD must come from a Kubernetes Secret, not plaintext env.value"
}

deny[msg] {
  is_workload
  labels := input.spec.template.metadata.labels
  not labels.app
  msg := "pod template must include required app label"
}

deny[msg] {
  is_workload
  labels := input.spec.template.metadata.labels
  not labels.owner
  msg := "pod template must include required owner label"
}

deny[msg] {
  is_workload
  labels := input.spec.template.metadata.labels
  not labels.environment
  msg := "pod template must include required environment label"
}

deny[msg] {
  is_workload
  labels := input.spec.template.metadata.labels
  not labels["compliance-scope"]
  msg := "pod template must include required compliance-scope label"
}
