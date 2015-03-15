-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: May 24, 2014 at 12:30 AM
-- Server version: 5.5.37-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `test`
--

-- --------------------------------------------------------

--
-- Table structure for table `email_disposable`
--

CREATE TABLE IF NOT EXISTS `email_disposable` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=310 ;

--
-- Dumping data for table `email_disposable`
--

INSERT INTO `email_disposable` (`id`, `domain`) VALUES
(1, '0815.ru0clickemail.com'),
(2, '0-mail.com'),
(3, '0wnd.net'),
(4, '0wnd.org'),
(5, '10minutemail.com'),
(6, '20minutemail.com'),
(7, '2prong.com'),
(8, '3d-painting.com'),
(9, '4warding.com'),
(10, '4warding.net'),
(11, '4warding.org'),
(12, '9ox.net'),
(13, 'a-bc.net'),
(14, 'ag.us.to'),
(15, 'amilegit.com'),
(16, 'anonbox.net'),
(17, 'anonymbox.com'),
(18, 'antichef.com'),
(19, 'antichef.net'),
(20, 'antispam.de'),
(21, 'baxomale.ht.cx'),
(22, 'beefmilk.com'),
(23, 'binkmail.com'),
(24, 'bio-muesli.net'),
(25, 'bobmail.info'),
(26, 'bodhi.lawlita.com'),
(27, 'bofthew.com'),
(28, 'brefmail.com'),
(29, 'bsnow.net'),
(30, 'bugmenot.com'),
(31, 'bumpymail.com'),
(32, 'casualdx.com'),
(33, 'chogmail.com'),
(34, 'cool.fr.nf'),
(35, 'correo.blogos.net'),
(36, 'cosmorph.com'),
(37, 'courriel.fr.nf'),
(38, 'courrieltemporaire.com'),
(39, 'curryworld.de'),
(40, 'cust.in'),
(41, 'dacoolest.com'),
(42, 'dandikmail.com'),
(43, 'deadaddress.com'),
(44, 'despam.it'),
(45, 'despam.it'),
(46, 'devnullmail.com'),
(47, 'dfgh.net'),
(48, 'digitalsanctuary.com'),
(49, 'discardmail.com'),
(50, 'discardmail.de'),
(51, 'disposableaddress.com'),
(52, 'disposeamail.com'),
(53, 'disposemail.com'),
(54, 'dispostable.com'),
(55, 'dm.w3internet.co.ukexample.com'),
(56, 'dodgeit.com'),
(57, 'dodgit.com'),
(58, 'dodgit.org'),
(59, 'dontreg.com'),
(60, 'dontsendmespam.de'),
(61, 'dump-email.info'),
(62, 'dumpyemail.com'),
(63, 'e4ward.com'),
(64, 'email60.com'),
(65, 'emailias.com'),
(66, 'emailias.com'),
(67, 'emailinfive.com'),
(68, 'emailmiser.com'),
(69, 'emailtemporario.com.br'),
(70, 'emailwarden.com'),
(71, 'enterto.com'),
(72, 'ephemail.net'),
(73, 'explodemail.com'),
(74, 'fakeinbox.com'),
(75, 'fakeinformation.com'),
(76, 'fansworldwide.de'),
(77, 'fastacura.com'),
(78, 'filzmail.com'),
(79, 'fixmail.tk'),
(80, 'fizmail.com'),
(81, 'frapmail.com'),
(82, 'garliclife.com'),
(83, 'gelitik.in'),
(84, 'get1mail.com'),
(85, 'getonemail.com'),
(86, 'getonemail.net'),
(87, 'girlsundertheinfluence.com'),
(88, 'gishpuppy.com'),
(89, 'goemailgo.com'),
(90, 'great-host.in'),
(91, 'greensloth.com'),
(92, 'greensloth.com'),
(93, 'gsrv.co.uk'),
(94, 'guerillamail.biz'),
(95, 'guerillamail.com'),
(96, 'guerillamail.net'),
(97, 'guerillamail.org'),
(98, 'guerrillamail.biz'),
(99, 'guerrillamail.com'),
(100, 'guerrillamail.de'),
(101, 'guerrillamail.net'),
(102, 'guerrillamail.org'),
(103, 'guerrillamailblock.com'),
(104, 'haltospam.com'),
(105, 'hidzz.com'),
(106, 'hotpop.com'),
(107, 'ieatspam.eu'),
(108, 'ieatspam.info'),
(109, 'ihateyoualot.info'),
(110, 'imails.info'),
(111, 'inboxclean.com'),
(112, 'inboxclean.org'),
(113, 'incognitomail.com'),
(114, 'incognitomail.net'),
(115, 'ipoo.org'),
(116, 'irish2me.com'),
(117, 'jetable.com'),
(118, 'jetable.fr.nf'),
(119, 'jetable.net'),
(120, 'jetable.org'),
(121, 'jnxjn.com'),
(122, 'junk1e.com'),
(123, 'kasmail.com'),
(124, 'kaspop.com'),
(125, 'klzlk.com'),
(126, 'kulturbetrieb.info'),
(127, 'kurzepost.de'),
(128, 'kurzepost.de'),
(129, 'lifebyfood.com'),
(130, 'link2mail.net'),
(131, 'litedrop.com'),
(132, 'lookugly.com'),
(133, 'lopl.co.cc'),
(134, 'lr78.com'),
(135, 'maboard.com'),
(136, 'mail.by'),
(137, 'mail.mezimages.net'),
(138, 'mail4trash.com'),
(139, 'mailbidon.com'),
(140, 'mailcatch.com'),
(141, 'maileater.com'),
(142, 'mailexpire.com'),
(143, 'mailin8r.com'),
(144, 'mailinator.com'),
(145, 'mailinator.net'),
(146, 'mailinator2.com'),
(147, 'mailincubator.com'),
(148, 'mailme.lv'),
(149, 'mailmetrash.com'),
(150, 'mailmoat.com'),
(151, 'mailnator.com'),
(152, 'mailnull.com'),
(153, 'mailzilla.org'),
(154, 'mbx.cc'),
(155, 'mega.zik.dj'),
(156, 'meltmail.com'),
(157, 'mierdamail.com'),
(158, 'mintemail.com'),
(159, 'mjukglass.nu'),
(160, 'mobi.web.id'),
(161, 'moburl.com'),
(162, 'moncourrier.fr.nf'),
(163, 'monemail.fr.nf'),
(164, 'monmail.fr.nf'),
(165, 'mt2009.com'),
(166, 'mx0.wwwnew.eu'),
(167, 'mycleaninbox.net'),
(168, 'myspamless.com'),
(169, 'mytempemail.com'),
(170, 'mytrashmail.com'),
(171, 'netmails.net'),
(172, 'neverbox.com'),
(173, 'no-spam.ws'),
(174, 'nobulk.com'),
(175, 'noclickemail.com'),
(176, 'nogmailspam.info'),
(177, 'nomail.xl.cx'),
(178, 'nomail2me.com'),
(179, 'nospam.ze.tc'),
(180, 'nospam4.us'),
(181, 'nospamfor.us'),
(182, 'nowmymail.com'),
(183, 'objectmail.com'),
(184, 'obobbo.com'),
(185, 'odaymail.com'),
(186, 'onewaymail.com'),
(187, 'ordinaryamerican.net'),
(188, 'owlpic.com'),
(189, 'pookmail.com'),
(190, 'privymail.de'),
(191, 'proxymail.eu'),
(192, 'punkass.com'),
(193, 'putthisinyourspamdatabase.com'),
(194, 'quickinbox.com'),
(195, 'rcpt.at'),
(196, 'recode.me'),
(197, 'recursor.net'),
(198, 'regbypass.comsafe-mail.net'),
(199, 'safetymail.info'),
(200, 'sandelf.de'),
(201, 'saynotospams.com'),
(202, 'selfdestructingmail.com'),
(203, 'sendspamhere.com'),
(204, 'sharklasers.com'),
(205, 'shieldedmail.com'),
(206, 'shiftmail.com'),
(207, 'skeefmail.com'),
(208, 'slopsbox.com'),
(209, 'slushmail.com'),
(210, 'smaakt.naar.gravel'),
(211, 'smellfear.com'),
(212, 'snakemail.com'),
(213, 'sneakemail.com'),
(214, 'sofort-mail.de'),
(215, 'sogetthis.com'),
(216, 'soodonims.com'),
(217, 'spam.la'),
(218, 'spamavert.com'),
(219, 'spambob.net'),
(220, 'spambob.org'),
(221, 'spambog.com'),
(222, 'spambog.de'),
(223, 'spambog.ru'),
(224, 'spambox.info'),
(225, 'spambox.us'),
(226, 'spamcannon.com'),
(227, 'spamcannon.net'),
(228, 'spamcero.com'),
(229, 'spamcorptastic.com'),
(230, 'spamcowboy.com'),
(231, 'spamcowboy.net'),
(232, 'spamcowboy.org'),
(233, 'spamday.com'),
(234, 'spamex.com'),
(235, 'spamfree.eu'),
(236, 'spamfree24.com'),
(237, 'spamfree24.de'),
(238, 'spamfree24.eu'),
(239, 'spamfree24.info'),
(240, 'spamfree24.net'),
(241, 'spamfree24.org'),
(242, 'spamgourmet.com'),
(243, 'spamgourmet.net'),
(244, 'spamgourmet.org'),
(245, 'spamherelots.com'),
(246, 'spamhereplease.com'),
(247, 'spamhole.com'),
(248, 'spamify.com'),
(249, 'spaminator.de'),
(250, 'spamkill.info'),
(251, 'spaml.com'),
(252, 'spaml.de'),
(253, 'spammotel.com'),
(254, 'spamobox.com'),
(255, 'spamspot.com'),
(256, 'spamthis.co.uk'),
(257, 'spamthisplease.com'),
(258, 'speed.1s.fr'),
(259, 'suremail.info'),
(260, 'tempalias.com'),
(261, 'tempe-mail.com'),
(262, 'tempemail.biz'),
(263, 'tempemail.com'),
(264, 'tempemail.net'),
(265, 'tempinbox.co.uk'),
(266, 'tempinbox.com'),
(267, 'tempomail.fr'),
(268, 'temporaryemail.net'),
(269, 'temporaryinbox.com'),
(270, 'tempymail.com'),
(271, 'thankyou2010.com'),
(272, 'thisisnotmyrealemail.com'),
(273, 'throwawayemailaddress.com'),
(274, 'tilien.com'),
(275, 'tmailinator.com'),
(276, 'tradermail.info'),
(277, 'trash-amil.com'),
(278, 'trash-mail.at'),
(279, 'trash-mail.com'),
(280, 'trash-mail.de'),
(281, 'trash2009.com'),
(282, 'trashmail.at'),
(283, 'trashmail.com'),
(284, 'trashmail.me'),
(285, 'trashmail.net'),
(286, 'trashmailer.com'),
(287, 'trashymail.com'),
(288, 'trashymail.net'),
(289, 'trillianpro.com'),
(290, 'tyldd.com'),
(291, 'tyldd.com'),
(292, 'uggsrock.com'),
(293, 'wegwerfmail.de'),
(294, 'wegwerfmail.net'),
(295, 'wegwerfmail.org'),
(296, 'wh4f.org'),
(297, 'whyspam.me'),
(298, 'willselfdestruct.com'),
(299, 'winemaven.info'),
(300, 'wronghead.com'),
(301, 'wuzupmail.net'),
(302, 'xoxy.net'),
(303, 'yogamaven.com'),
(304, 'yopmail.com'),
(305, 'yopmail.fr'),
(306, 'yopmail.net'),
(307, 'yuurok.com'),
(308, 'zippymail.info'),
(309, 'zoemail.com');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;