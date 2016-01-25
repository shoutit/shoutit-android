#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

import argparse
import sys
import urlparse
import urllib2
import urllib
import json
import os
import mimetypes

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'

handler = urllib2.HTTPSHandler(debuglevel=0)
opener = urllib2.build_opener(handler)


def main():
    parser = argparse.ArgumentParser(description='Manage task.')
    parser.add_argument('--token', dest='token', required=True, nargs="?",
                        type=str, help='token')
    parser.add_argument('--build-name', dest='build_name', required=True, nargs="?",
                        type=str, help='build name')
    parser.add_argument('--base-url', dest='base_url', nargs="?",
                        type=str, help='base url', default="https://auto-close.appspot.com/")
    parser.add_argument('files', metavar='FILES', type=str, nargs="+",
                        help='files to upload')
    parser.add_argument('--final', dest="final", default=False, action="store_true")
    args = parser.parse_args()

    response = execute_json(urlparse.urljoin(args.base_url, "build"), {
        "token": args.token,
        "build_name": args.build_name
    })

    mime = mimetypes.MimeTypes()

    project_build_key = response["project_build"]

    for path in args.files:
        if os.path.isfile(path):
            send_file(args, os.getcwd(), path, mime, project_build_key)
        elif os.path.isdir(path):
            for root, dir_names, filenames in os.walk(path):
                for filename in filenames:
                    relative_path = os.path.join(root, filename)
                    send_file(args, path, relative_path, mime, project_build_key)
        else:
            raise Exception("Unknown type of: %s" % (path,))

    commit_url = "%sbuild/%s/commit" % (args.base_url, project_build_key)
    execute_json(commit_url, {
        "final": args.final,
        "token": args.token
    })


def send_file(args, base_path, path, mime, project_build_key):
    filename = os.path.relpath(path, base_path)
    content_type = mime.guess_type(urllib.pathname2url(filename))[0] or ""
    artifact_response = execute_json(urlparse.urljoin(args.base_url, "artifact"), {
        "token": args.token,
        "project_build": project_build_key,
        "artifact_name": filename,
        "content_type": content_type
    })
    upload_data = artifact_response["upload"]
    upload_url = upload_data["url"]
    execute_binary(upload_url, path, content_type)
    download_url = artifact_response["download"]
    print "Serving artifacts %s -> %s (%s)" % (filename, download_url, content_type)


def execute(url, data=None, headers=None, method="POST"):
    if headers is None:
        headers = {}
    request = urllib2.Request(url.encode("utf-8"), data, headers)
    try:
        request.get_method = lambda: method
        response = opener.open(request)
        return response.read()
    except urllib2.HTTPError as e:
        print >> sys.stderr, "Response from server: %s" % e.read()
        raise e


def execute_json(url, data):
    headers = {
        "Content-Type": "application/json"
    }
    return json.loads(execute(url, json.dumps(data), headers))


def execute_binary(url, filename, content_type):
    with open(filename, "rb") as file_content:
        data = file_content.read()
        headers = {
            "Content-Type": content_type
        }
        execute(url, data=data, headers=headers, method="PUT")


if __name__ == '__main__':
    main()
