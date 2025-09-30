import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Container,
  Card,
  CardContent,
  Typography,
  Avatar,
  Chip,
  Rating,
  Button,
  Grid,
  Divider,
  Paper,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Alert,
  IconButton
} from '@mui/material';
import {
  Phone,
  WhatsApp,
  Message,
  LocationOn,
  AccessTime,
  AttachMoney,
  Work,
  ArrowBack,
  Star,
  Verified
} from '@mui/icons-material';
import client from '../../api/client';
import { auth } from '../../store/auth';

interface Review {
  id: number;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
  verified: boolean;
}

interface Technician {
  id: number;
  fullName: string;
  phone: string;
  email: string;
  summary: string;
  description: string;
  profilePhotoUrl?: string;
  averageRating: number;
  totalReviews: number;
  baseServiceFee?: number;
  currency: string;
  experienceYears: number;
  region: {
    governorate: string;
    city: string;
  };
  fields: Array<{ id: number; name: string }>;
  availabilityStatus: 'AVAILABLE' | 'BUSY' | 'OFFLINE';
  allowDirectCalls: boolean;
  allowWhatsApp: boolean;
  allowInAppMessages: boolean;
  whatsappNumber?: string;
  workingHoursStart?: string;
  workingHoursEnd?: string;
  workingDays?: string[];
  isEmergencyService: boolean;
  isVerified: boolean;
  certification?: string;
}

export default function TechnicianProfile() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const userAuth = auth.get();

  const { data, isLoading, error } = useQuery({
    queryKey: ['technician', id],
    queryFn: async () => {
      const response = await client.get(`/public/technicians/${id}`);
      return response.data;
    }
  });

  const handleContact = async (method: string) => {
    if (!userAuth.token) {
      navigate('/login');
      return;
    }

    try {
      const response = await client.post(`/user/technicians/${id}/contact`, {
        contactMethod: method
      });

      if (response.data.allowed) {
        switch (method) {
          case 'call':
            window.open(`tel:${response.data.phoneNumber}`);
            break;
          case 'whatsapp':
            window.open(response.data.whatsappLink, '_blank');
            break;
          case 'message':
            console.log('Message thread:', response.data.messageThreadId);
            break;
        }
      } else {
        alert(response.data.message || 'Contact method not available');
      }
    } catch (err) {
      console.error('Contact error:', err);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !data) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Alert severity="error">
          {i18n.language === 'ar' ? 'الفني غير موجود' : 'Technician not found'}
        </Alert>
      </Container>
    );
  }

  const technician: Technician = data.technician;
  const reviews: Review[] = data.reviews || [];

  const getAvailabilityColor = (status: string) => {
    switch (status) {
      case 'AVAILABLE': return 'success';
      case 'BUSY': return 'warning';
      case 'OFFLINE': return 'error';
      default: return 'default';
    }
  };

  const getAvailabilityText = (status: string) => {
    if (i18n.language === 'ar') {
      switch (status) {
        case 'AVAILABLE': return 'متاح';
        case 'BUSY': return 'مشغول';
        case 'OFFLINE': return 'غير متصل';
        default: return status;
      }
    }
    return status;
  };

  return (
    <Container maxWidth="lg">
      <Button
        startIcon={<ArrowBack />}
        onClick={() => navigate(-1)}
        sx={{ mb: 3 }}
      >
        {i18n.language === 'ar' ? 'رجوع' : 'Back'}
      </Button>

      <Grid container spacing={3}>
        {/* Main Profile Card */}
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent sx={{ p: 4 }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 3 }}>
                <Avatar
                  src={technician.profilePhotoUrl}
                  sx={{ width: 100, height: 100, mr: 3 }}
                >
                  {technician.fullName.charAt(0)}
                </Avatar>
                
                <Box sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <Typography variant="h4" sx={{ fontWeight: 'bold' }}>
                      {technician.fullName}
                    </Typography>
                    {technician.isVerified && (
                      <Verified color="primary" />
                    )}
                  </Box>
                  
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <Rating value={technician.averageRating} readOnly precision={0.5} />
                    <Typography variant="body2" color="text.secondary">
                      {technician.averageRating.toFixed(1)} ({technician.totalReviews} {i18n.language === 'ar' ? 'تقييم' : 'reviews'})
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                    <Chip
                      label={getAvailabilityText(technician.availabilityStatus)}
                      color={getAvailabilityColor(technician.availabilityStatus)}
                      size="small"
                    />
                    {technician.isEmergencyService && (
                      <Chip
                        label={i18n.language === 'ar' ? 'خدمة طوارئ' : 'Emergency Service'}
                        color="error"
                        size="small"
                      />
                    )}
                  </Box>

                  <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                    {technician.summary}
                  </Typography>
                </Box>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* Fields */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Work />
                  {i18n.language === 'ar' ? 'المجالات' : 'Fields'}
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  {technician.fields.map(field => (
                    <Chip key={field.id} label={field.name} variant="outlined" />
                  ))}
                </Box>
              </Box>

              {/* Description */}
              {technician.description && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    {i18n.language === 'ar' ? 'الوصف' : 'Description'}
                  </Typography>
                  <Typography variant="body1" sx={{ whiteSpace: 'pre-line' }}>
                    {technician.description}
                  </Typography>
                </Box>
              )}

              <Divider sx={{ my: 3 }} />

              {/* Details */}
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                    <LocationOn color="action" />
                    <Typography variant="body2">
                      {technician.region.governorate}, {technician.region.city}
                    </Typography>
                  </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                    <Work color="action" />
                    <Typography variant="body2">
                      {technician.experienceYears} {i18n.language === 'ar' ? 'سنة خبرة' : 'years experience'}
                    </Typography>
                  </Box>
                </Grid>
                {technician.workingHoursStart && technician.workingHoursEnd && (
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                      <AccessTime color="action" />
                      <Typography variant="body2">
                        {technician.workingHoursStart} - {technician.workingHoursEnd}
                      </Typography>
                    </Box>
                  </Grid>
                )}
                {technician.baseServiceFee && (
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                      <AttachMoney color="action" />
                      <Typography variant="body2">
                        {technician.baseServiceFee} {technician.currency}
                      </Typography>
                    </Box>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>

          {/* Reviews Section */}
          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Star />
                {i18n.language === 'ar' ? 'التقييمات' : 'Reviews'} ({reviews.length})
              </Typography>
              
              {reviews.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  {i18n.language === 'ar' ? 'لا توجد تقييمات بعد' : 'No reviews yet'}
                </Typography>
              ) : (
                <List>
                  {reviews.map((review) => (
                    <ListItem key={review.id} alignItems="flex-start" divider>
                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                            <Typography variant="subtitle2">{review.userName}</Typography>
                            {review.verified && (
                              <Chip label="Verified" size="small" color="success" />
                            )}
                            <Rating value={review.rating} size="small" readOnly />
                          </Box>
                        }
                        secondary={
                          <>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                              {review.comment}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {new Date(review.createdAt).toLocaleDateString(i18n.language)}
                            </Typography>
                          </>
                        }
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Contact Card */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
            <Typography variant="h6" gutterBottom>
              {i18n.language === 'ar' ? 'اتصل بالفني' : 'Contact Technician'}
            </Typography>
            
            {technician.baseServiceFee && (
              <Box sx={{ mb: 3 }}>
                <Typography variant="body2" color="text.secondary">
                  {i18n.language === 'ar' ? 'سعر الخدمة' : 'Service Fee'}
                </Typography>
                <Typography variant="h4" color="primary" sx={{ fontWeight: 'bold' }}>
                  {technician.baseServiceFee} {technician.currency}
                </Typography>
              </Box>
            )}

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {technician.allowDirectCalls && (
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<Phone />}
                  onClick={() => handleContact('call')}
                  size="large"
                >
                  {i18n.language === 'ar' ? 'اتصال' : 'Call'}
                </Button>
              )}

              {technician.allowWhatsApp && (
                <Button
                  fullWidth
                  variant="contained"
                  sx={{ bgcolor: '#25D366', '&:hover': { bgcolor: '#20BA5A' } }}
                  startIcon={<WhatsApp />}
                  onClick={() => handleContact('whatsapp')}
                  size="large"
                >
                  WhatsApp
                </Button>
              )}

              {technician.allowInAppMessages && (
                <Button
                  fullWidth
                  variant="outlined"
                  startIcon={<Message />}
                  onClick={() => handleContact('message')}
                  size="large"
                >
                  {i18n.language === 'ar' ? 'رسالة' : 'Message'}
                </Button>
              )}
            </Box>

            {!userAuth.token && (
              <Alert severity="info" sx={{ mt: 2 }}>
                {i18n.language === 'ar' 
                  ? 'سجل الدخول للتواصل مع الفني'
                  : 'Please login to contact the technician'}
              </Alert>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}
