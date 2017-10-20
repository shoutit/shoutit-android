#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

from __future__ import print_function

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'


import argparse
import sys
import urlparse
import urllib2
import json
import tarfile
import StringIO


def main():
    parser = argparse.ArgumentParser(description='Manage task.')
    parser.add_argument('--token', dest='token', required=True, nargs="?",
                        type=str, help='token')
    parser.add_argument('--base-url', dest='base_url', nargs="?",
                        type=str, help='base url', default="https://builds.appunite.com/")
    parser.add_argument('files', metavar='FILES', type=str, nargs="+",
                        help='files to upload')
    args = parser.parse_args()

    response = execute_json(urlparse.urljoin(args.base_url, "build/keys"), {
        "token": args.token,
    })

    url = response["upload"]["url"]
    version = response["version"]
    tar = create_tar(args.files)
    upload(url, tar)
    print("Version of keys: %s" % version)

handler = urllib2.HTTPSHandler(debuglevel=0)
opener = urllib2.build_opener(handler)


def upload(url, stream):
    headers = {
        "Content-Type": ""
    }
    request = urllib2.Request(url.encode('utf-8'), stream, headers)
    request.get_method = lambda: 'PUT'
    return execute_http(request)


def create_tar(files):
    stream = StringIO.StringIO()
    tar = tarfile.open(fileobj=stream, mode='w|bz2')
    for f in files:
      tar.add(f)
    tar.close()
    return stream.getvalue()


def execute_json(base_url, data):
    headers = {
        "Content-Type": "application/json"
    }
    request = urllib2.Request(base_url, json.dumps(data), headers)
    return json.loads(execute_http(request))


def execute_http(request):
    try:
        response = opener.open(request)
        return response.read()
    except urllib2.HTTPError as e:
        print("Response from server: %s" % e.read(), file=sys.stderr)
        raise e


if __name__ == '__main__':
    main()
