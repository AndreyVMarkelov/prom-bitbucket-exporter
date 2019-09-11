# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

[Tags on this repository](https://github.com/AndreyVMarkelov/prom-bitbucket-exporter/releases)

## [Unreleased]

## [1.0.12] (v5.x-6.7.x)

- bitbucket_repo_move_count
- Fix 6.6.1 compatibility

## [1.0.11] (v5.x-6.3.x)

- Fixed performance issues
- Added new metric: bitbucket_allowed_users_gauge (maximum allowed with current license)
- Added new metric: bitbucket_license_expiry_days_gauge (days before license will expire)
- Remove metric: bitbucket_all_users_gauge (caused performance issues. No needed. Actually was the users in AD)
