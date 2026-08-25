# Contributing to Photo Uploader

Thank you for your interest in contributing to Photo Uploader.
By contributing, you agree that your submissions will be licensed
under the [Apache License 2.0](LICENSE).

## Before You Start

Please open an issue to discuss your proposed change before starting
work on a significant contribution. This avoids effort being spent
on changes that may not align with the project's direction.

## Contribution Workflow

1. Fork the repository.
2. Create a branch from `main` for your change:
   ```git checkout -b fix/my-bug-fix```

   Use a descriptive branch name prefixed with `feat/`, `fix/`, or `docs/`.
3. Make your changes. Keep commits focused — one logical change per commit.
4. Sign off every commit (see below).
5. Open a pull request against `main`.
6. Address any review feedback.
7. A maintainer will merge the pull request once approved.

## Commit Sign-off (DCO)

This project requires all commits to be signed off under the
[Developer Certificate of Origin](https://developercertificate.org).
This certifies that you wrote the code or have the right to submit
it under the Apache 2.0 license.

Add a sign-off to every commit using the `-s` flag:

```git commit -s -m "fix: correct Dutch translation for error_remote_file_not_found"```

This adds the following line to your commit message:

```Signed-off-by: Your Name your.email@example.com```

Pull requests containing unsigned commits will not be merged and
will be blocked by an automated check.

## Code Style and Architecture

- Follow the architecture defined in `ARCHITECTURE.md`.
- All new code must be in Kotlin. No Java.
- New features require a corresponding entry in `CHANGELOG.md`
  under `[Unreleased]` in the same commit as the feature.
- Copyright header required on every new `.kt` file:
```
/*
 * Copyright (c) <year> True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

## Pull Request Requirements

The following must be satisfied before a pull request is opened:
- [ ] All commits are signed off (`Signed-off-by` present).
- [ ] Code compiles and all existing functionality works.
- [ ] `CHANGELOG.md` is updated if the change is user-facing.
- [ ] No new dependencies are added without prior discussion in an issue.

The following is required before a pull request is merged:
- [ ] Reviewed and approved by a maintainer.