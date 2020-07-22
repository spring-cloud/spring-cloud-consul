# Linode provider block. Installs Linode plugin.
provider "linode" {
	token = "${var.token}"
}

variable "region" {
	description = "This is the location where the Linode instance is deployed."
}

/* A multi
   line comment. */
resource "linode_instance" "example_linode" {
	image = "linode/ubuntu18.04"
	label = "example-linode"
	region = "${var.region}"
	type = "g6-standard-1"
	authorized_keys = [ "my-key" ]
	root_pass = "example-password"
}
