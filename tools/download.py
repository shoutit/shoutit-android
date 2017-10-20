#!/usr/bin/env python
#
# Copyright 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>

__author__ = 'Jacek Marchwicki <jacek.marchwicki@gmail.com>'


import argparse
import urlparse
import urllib2
import tarfile
import StringIO


def main():
    parser = argparse.ArgumentParser(description='Manage task.')
    parser.add_argument('--token', dest='token', required=True, nargs="?",
                        type=str, help='token')
    parser.add_argument('--key-version', dest='key_version', required=False, nargs="?",
                        type=str, help='key_version')
    parser.add_argument('--base-url', dest='base_url', nargs="?",
                        type=str, help='base url', default="https://builds.appunite.com/")
    args = parser.parse_args()

    if args.key_version:
        request = "build/keys?token=%s&version=%s" % (args.token, args.key_version)
    else:
        request = "build/keys?token=%s" % (args.token, )

    url = urlparse.urljoin(args.base_url, request)
    tar = download(url)
    untar(tar)

handler = urllib2.HTTPSHandler(debuglevel=0)
opener = urllib2.build_opener(handler)

def untar(content):
    stream = StringIO.StringIO(content)
    tar = tarfile.open(fileobj=stream, mode='r|bz2')
    tar.extractall()
    tar.list()
    tar.close()

def download(url):
    request = urllib2.Request(url.encode('utf-8'))
    return execute_http(request)

def execute_http(request):
    try:
        response = opener.open(request)
        return response.read()
    except urllib2.HTTPError as e:
        print >> sys.stderr, "Response from server: %s" % e.read()
        raise e


if __name__ == '__main__':
    main()
