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
set -euo pipefail

REPO="windbow27/kornblume"
FILES=(items.json stages.json formulas.json arcanists.json psychubes.json)
TARGET="${1:-build/upstream-snapshots}"

# label:commit — the adapter reads a directory per snapshot, named by label.
SNAPSHOTS=(
  "3.3:d49efab2a18f"
  "3.5:8b40541a9c42"
)

for snapshot in "${SNAPSHOTS[@]}"; do
  label="${snapshot%%:*}"
  commit="${snapshot##*:}"
  dir="${TARGET}/${label}"
  mkdir -p "${dir}"

  for file in "${FILES[@]}"; do
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
