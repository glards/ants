#!/usr/bin/ruby1.9.1

require 'rubygems'
require 'zip/zip'

scriptRoot = File.dirname(File.expand_path(__FILE__))

scriptRoot << "/" if scriptRoot[-1] != '/'

src = scriptRoot + "../src/main/java/"

zipFile = scriptRoot + "../build/bot.zip"

basedir = File::expand_path(src)
basedir << "/" if basedir[-1] != '/'

puts File::expand_path(zipFile)

File.delete(zipFile) if File.exists?(zipFile)

Zip::ZipFile.open(zipFile, Zip::ZipFile::CREATE) do |z|
  Dir.glob(basedir + "**").each do |f|
    file = File::expand_path(f)
    ofs = file.length - basedir.length
    localName = file[-ofs .. -1]
    z.add(localName,f)
  end
end
