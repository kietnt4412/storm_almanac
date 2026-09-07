#!/usr/bin/env bash
#
# Downloads Reverse: 1999 data snapshots for the reverse-1999 parser adapter.
#
# WHY THIS IS A SCRIPT AND NOT A TEST FIXTURE
#
# The Kornblume repository carries no LICENSE file, which is all rights reserved
# by default rather than permission — absence of a licence is not a grant. We may
# read the data to build and check an adapter; we may not vendor it here. So the
# snapshots live outside the repository, this script fetches them on demand, and
# .gitignore keeps the output directory out of the index. See open question Q3 in
# TRACKER.md and section 5 of docs/prior-art.md.
#
# The consequence is deliberate and is written down rather than worked around:
# RealUpstreamPatchTest skips itself when the snapshots are absent, so CI is
# green without them and the test is real when they are there.
#
#   ./tools/fetch-upstream.sh                    # into build/upstream-snapshots
#   ./tools/fetch-upstream.sh /some/other/dir
#
# Note that the default lands under build/, so `./gradlew clean` removes it and
# this has to be run again. That is the right trade: the alternative is a
# directory outside build/ that a stale copy can hide in.
#
# Two snapshots are fetched, taken from the upstream's own history at the commits
# where it recorded a patch update, so that "a patch that changes something" is a
# real patch and not one we composed:
#
#   3.3  d49efab2a18f  2025-12-22
#   3.5  8b40541a9c42  2026-03-17
#
# WHICH STAGE FILE, AND WHY IT IS NOT ONLY stages.json
#
# The upstream ships several stage tables and its own planner reads exactly one
# of them: the newest public/data/stages<major>_<minor>_greedy.json, which holds
# a drop count per item and the number of sampled runs behind it. stages.json is
# the older shape — a bare proportion, no sample size — and at both of the pinned
# commits it covers chapters 1 to 4 only. Fetching just that file gave us a
# well-formed bundle over a third of the game and nothing said so; every stage
# the community names as best to farm was missing from it.
#
# The version in the filename lags the patch label, because the drop data is
# resampled on its own schedule rather than per patch. So the file is pinned per
# snapshot rather than derived from the label:
#
#   3.3 -> stages3_0_greedy.json
#   3.5 -> stages3_3_greedy.json
#
set -euo pipefail

REPO="windbow27/kornblume"
FILES=(items.json stages.json formulas.json arcanists.json psychubes.json)
TARGET="${1:-build/upstream-snapshots}"

# label:commit:stage-table — the adapter reads a directory per snapshot, named
# by label, and prefers the sampled stage table when it finds one.
SNAPSHOTS=(
  "3.3:d49efab2a18f:stages3_0_greedy.json"
  "3.5:8b40541a9c42:stages3_3_greedy.json"
)

for snapshot in "${SNAPSHOTS[@]}"; do
  label="${snapshot%%:*}"
  rest="${snapshot#*:}"
  commit="${rest%%:*}"
  sampled="${rest##*:}"
  dir="${TARGET}/${label}"
  mkdir -p "${dir}"

  for file in "${FILES[@]}" "${sampled}"; do
    url="https://raw.githubusercontent.com/${REPO}/${commit}/public/data/${file}"
    status=$(curl -sS -o "${dir}/${file}" -w "%{http_code}" "${url}")
    if [ "${status}" != "200" ]; then
      echo "FAILED ${label}/${file}: HTTP ${status} from ${url}" >&2
      exit 1
    fi
    printf '  %-8s %-16s %8d bytes\n' "${label}" "${file}" "$(wc -c < "${dir}/${file}")"
  done
done

echo
echo "Fetched into ${TARGET}. Not for redistribution — see the header of this script."
echo
echo "Convert one, without touching the database:"
echo "  java -jar app/build/libs/storm-almanac.jar \\"
echo "    --gamedata=adapt reverse-1999 ${TARGET}/3.3 0 3.3 build/reverse-1999-3.3.json"
echo
echo "Run the tests that need it:"
echo "  ./gradlew :app:test -Dstorm-almanac.upstream=\$(pwd)/${TARGET}"
